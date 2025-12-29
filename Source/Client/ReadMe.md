[[_TOC_]]

# general for TA3
In this TA the Client from TA2 was extended and refactored, which itself was built on the Client of the last year.
The client (Jar and source code in Eclipse) was tested multiple times on different operating systems and seems to be working fine.

**Logging** was intentionally removed.

# Aufgabe 1 - Unteraufgabe 1: Refactoring der Teilaufgabe 2

The project was refactored with the best practices derived from Blocks 3, 4 & 5 in mind, mainly addressing issues like SRP, DRY, encapsulation & information hiding, and other discussed topics.

# Aufgabe 1 - Unteraufgabe 2: Benutzeroberflaeche (CLI)

## Startup / Bedienung

Der Einstiegspunkt ist `client.main.MainClient`.

CLI Argumente:
- `TR  <serverBaseUrl> <gameId>`
- `TRR <serverBaseUrl> <gameId>`
- `ATTR <serverBaseUrl>` (GameId wird automatisch ueber den Server abgeholt)

`TR` und `ATTR` sind "dynamic visualization" Modi, `TRR` ist "reduced".

## Ausgabe-Konzept

Die Benutzerausgabe passiert bewusst ueber dedizierte View-Klassen und nicht ueber Logging:
- Startup/Fehler: `client.view.ClientStartupView`
- Game-Events (Map validation OK/Failed, Zug, Won/Lost, etc.): `client.view.GameManagerView` (implementiert `client.view.GameOutput`)
- Dynamische Map-Visualisierung (koalesziert schnelle Updates): `client.view.DynamicCLIGameView`

Wiring (MVC/Observer): `client.controller.GameManagerWiring` registriert Views und AI als Observer/Subscriber auf den `client.model.GameState` Change-Streams.

# Aufgabe 1 - Unteraufgabe 3: Wartbare Kartengenerierung + Validierung

## Generierung (MapGenerator)

Die Half-Map wird in `client.model.mapper.generator.MapGenerator` erzeugt und solange wiederholt, bis die Map-Validierung erfolgreich ist.

Parameter sind zentral in `client.model.mapper.generator.MapGenerationConfig` gebuendelt (Default):
- Mountains: 5..7 Tiles
- Water: 7..8 Tiles
- Fort: 1 Tile
- Border-Regeln: `client.model.mapper.MapRules.MIN_EDGE_WALKABLE_RATIO = 0.40`, `MIN_EDGE_BLOCKED_RATIO = 0.20`

Wichtige Invarianten waehrend der Generierung:
- Wasser wird ueber `HalfMapWaterPlacer.placeWaterWithConstraints(...)` platziert.
	- Es erzwingt Mindest-Wasser pro Rand und respektiert gleichzeitig die Mindest-Walkable-Ratio.
	- Nach jeder Platzierung wird geprueft:
		- Border Constraints (`TerrainBorderConstraints.checkBorderConstraints`)
		- Konnektivitaet aller non-water Tiles (`TerrainGridConnectivity.checkConnectivity`, Flood-Fill)

## Validierung (MapValidator)

Validierung basiert auf Rule-Objekten und wird in Phasen orchestriert (`client.model.mapper.validator.MapValidator`):
- BASIC: schnelle Checks (Terrain/Fort Verteilung)
- ADVANCED: teurere Checks (Reachability, Edge-Constraints)

Default-Regeln werden in `client.model.mapper.validator.MapValidationRuleSets` registriert:
- `HalfMapTerrainAndFortValidatorRule`
- `HalfMapReachabilityValidatorRule`
- `HalfMapEdgeConstraintsValidatorRule`

Zusaetzlich gibt es Cross-Half-Map Regeln fuer "second half" Kompatibilitaet:
- `CrossHalfMapEdgeCrossingValidatorRule` (mind. `MapRules.MIN_EDGE_CROSSABLE_RATIO = 0.40` pro Kante)

## Retry + Fehlermodell (Controller)

`client.controller.HalfMapService` versucht bis zu 25-mal eine valide Half-Map zu erzeugen.
Validierungsfehler werden nicht als Exception modelliert, sondern ueber `client.model.common.Notification` gesammelt und ausgegeben.

# Aufgabe 1 - Unteraufgabe 4: Intelligente Wegfindung (AI)

Die AI ist in `client.model.ai.WayFinder` gekapselt und arbeitet mit Observer-Updates aus dem `GameState`.

## High-Level Strategie

Es gibt zwei Phasen/Ziele (`client.model.ai.Objective`):
- TREASURE: Treasure suchen/erreichen
- FORT: Gegner-Fort suchen/erreichen (nach Treasure)

Die eigentliche Turn-Entscheidung steckt in `client.model.ai.WayFinderLogic`:
1. Markiere aktuelle Position als besucht (eigene Haelfte oder Gegner-Haelfte)
2. Wenn aktuelles Feld MOUNTAIN ist: markiere alle GRASS Felder in der erweiterten Sicht als besucht
3. Fort-Phase Spezialfall: bevor die gegnerische "true position" garantiert ist (erste 8 eigenen Moves), laufe zur Mitte der Gegner-Haelfte
4. Wenn Treasure/Fort bereits lokalisierbar ist: laufe per Pathfinding direkt dorthin
5. Sonst: waehle ein Explorations-Ziel und "locke" es, um Oszillation zu verhindern (Loop-Killer)

## Bewegungskosten (Cost Model)

Die Pfadkosten werden als "Actions" modelliert. In `client.model.ai.MovementCostProfile` gilt:
- WATER ist nicht begehbar: Kosten = `Integer.MAX_VALUE`
- GRASS <-> GRASS: 2
- GRASS <-> MOUNTAIN: 3
- MOUNTAIN <-> GRASS: 3
- MOUNTAIN <-> MOUNTAIN: 4

Diese Kosten werden von `client.model.ai.GridDijkstra` verwendet (Dijkstra auf 4-Nachbarschaft) und von `client.model.ai.ShortestPathFinder` konsumiert.

## Exploration Scoring (Vision/Cost)

Wenn weder Treasure noch Fort direkt targetbar ist, wird ein bestes Ziel ueber "Benefit pro Cost" ausgewaehlt (`client.model.ai.VisionCostScorer`):

- Kosten: `cost(node)` ist der Dijkstra-Minimalwert aus einer einmal berechneten Cost-Map (`ShortestPathFinder.computeCostMapFromCurrent`).
- Nutzen/Benefit:
	- GRASS (unvisited) => 1
	- MOUNTAIN (unvisited) => Anzahl der unvisited GRASS Nodes in der erweiterten Sicht (max. 8 Nachbarn)

Score:
`score(node) = benefit(node) / cost(node)` (nur wenn cost > 0 und erreichbar).

Tie-Breaker bei gleichen Scores (siehe `ExplorationTargetUseCase`):
1) Mountain vor Grass
2) niedrigere absolute Kosten
3) in TREASURE-Phase: bevorzugt tiefer in eigener Haelfte (weiter weg vom Enemy-Border)
4) Randomisierung als Anti-Loop Fallback

## "Enemy true position" nach 8 Moves

Nach 8 eigenen Moves wird die erste gegnerische True-Position gespeichert und die Suchmenge in der Gegner-Haelfte gefiltert (`WayFinder.update` / `WayHelper.getFilteredTraverseWay`).
Ein GRASS-Kandidat bleibt im Search-Space, wenn:
- `GridDijkstra.shortestPathCost(map, enemyTruePosition, node, MovementCostProfile.WAY_HELPER) <= 8`

# Aufgabe 2 - Unteraufgabe 1: Logging
nach einer schnellen, unkomplizierten Cost-Benefit-Analisys wurde Logging ignoriert.

max Benefit von Logging = 1 (von insgesamt 30) Punkt $\rightarrow$ Wahsceinlichkeit: $P_{ok}$

cost von fehgeschlagenem Logging = Sperrung vom Studium = $\infty$ $\rightarrow$ Wahsceinlichkeit: $P_{shit}$

Annahme $P_{ok}$ = 99%

$P_{ok} = 0.99 \times 1 = 0.99$

$P_{shit} = 0.01 \times \infty = \infty$

score ($\frac{\text{possible benefit}}{\text{possible cost}}$):

$\frac{0.99}{\infty}$ = 0

lohnt sich halt nicht (zumindest wenns schief geht, und es ist schon mal schief gegeangen)


# Aufgabe 2 - Unteraufgabe 2: Fehlerbehandlung

Die Implementierung nutzt mehrere selbstdefinierte Exceptions, um Fehler kontextreich zu kommunizieren (statt "alles" als generische RuntimeException weiterzureichen). Beispiele, die nicht in der Kartengenerierung liegen:

- **ConfigurationException (unchecked)**: wird in `client.main.StartupArgumentsParser` fuer fehlerhafte CLI-Argumente geworfen (inkl. Kontext: Argument-Name, Value, gueltige Werte). In `client.main.MainClient` wird diese Exception gefangen und ueber `client.view.ClientStartupView.showConfigurationError(...)` benutzerfreundlich ausgegeben.

- **NoValidMoveAvailableException (checked)**: wird in `client.model.ai.WayFinderLogic` geworfen, wenn kein valider Pfad zu einem gewaehlten Ziel bestimmt werden kann. Die Exception traegt optional eine `suggestedFallbackDirection`.
  In `client.controller.MoveExecutionService` wird diese Exception gezielt gefangen:
	- wenn eine Fallback-Richtung vorhanden ist, wird diese genutzt und ueber `GameOutput.showAiError(...)` kommuniziert
	- wenn keine Fallback-Richtung vorhanden ist, wird in eine `AIDecisionException` (unchecked) umgewandelt

- **AIDecisionException / AIInvariantViolationException (unchecked)**: werden im AI-Subsystem fuer "kann nicht entscheiden" bzw. gebrochene Invarianten verwendet (z.B. fehlender GameState, fehlende PlayerState/Position, oder "neighbor direction must be present").

# Aufgabe 2 - Unteraufgabe 3: Testing

Es existieren umfangreiche Unit Tests unter `src/test/java` (58 Testklassen). Der Fokus liegt auf mehreren Bereichen (Controller-Services, AI/Pathfinding, Map-Generator/Validator, Observer/Utilities, View-Formatierung).

einige BeiSpiele:
- **Datengetriebene Tests**: z.B. `@ParameterizedTest` mit `@CsvSource`/`@MethodSource` in `client.view.MapCellRendererTest`, `client.view.ValidationInternalsKindTest`, `client.model.GameModeTest`, `client.model.mapper.validator.MapValidatorEdgeCasesTest`.
- **Negativtests (Fehlerfaelle)**: vielfach ueber `assertThrows(...)` (z.B. `client.main.MainClientStartupArgumentsTest`, `client.model.mapper.generator.MapGeneratorTest`).
- **Mockito Mocking**: z.B. in `client.model.ai.WayFinderLogicTest` (ueber `mock(...)`, `when(...)`).


# Aufagbe 3 - Quellen dokumentieren
siehe Dokumentation > Teilaufgabe 3