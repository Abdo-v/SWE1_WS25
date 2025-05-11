[[_TOC_]]

# Wofür wird GIT verwendet?

In dieser Lehrveranstaltung werden Ihre Übungsausarbeitungen, sofern nicht explizit anders angeführt, über GitLab abgegeben bzw. eingereicht. Wichtig hierbei ist, dass wir den letzten gepushten Commit Ihres Masterbranches als maßgeblich für die Bewertung und die Bestimmung der von Ihnen gewählten Deadline ansehen. Die jeweiligen Punkte und Prüfungsergebnisse sind anschließend in Moodle ersichtlich. 

**Ändern Sie nicht den Namen des Masterbranches**: Dieser muss den Namen `master` tragen. Nur Daten, die vor der Abgabedeadline im Masterbranch liegen (daher Commit samt Push *vor* der Deadline) werden während potentieller Abgabegespräche und der Bewertung berücksichtigt. Sie können eigene Branches anlegen, vergessen Sie dann aber nicht auf einen finalen Merge (unter Beibehaltung aller individuellen Commits & Commitmessages, daher *kein* squash) *vor* der Deadline in den `master` Branch.

# Wie erhalte ich lokalen Zugriff auf dieses Repository?

Um optimal mit diesem Repository zu arbeiten sollten Sie es auf Ihr lokales Arbeitsgerät spiegeln. Verwenden Sie hierzu den Befehl `git clone URLIhresRepositories`. Die URL Ihres Repositories finden Sie im Kopf dieser Webseite rechts vom Namen des Repositories. Um diese zu erhalten drücken Sie auf den blauen mit `Clone` beschrifteten Knopf. Wählen Sie die mittels `Clone with HTTPS` bereitgestellte URL. Diese sollte vergleichbar sein zu `https://git01lab.cs.univie.ac.at/.....`. 

**Probleme mit den Zertifikaten**: Falls Sie beim clonen Ihres Git Repositories Probleme gemeldet bekommen, die mit der Prüfung der Zertifikate in Verbindung stehen ist es eine schnelle Lösung diese abzuschalten. Hierzu kann folgender Befehl verwendet werden:  `git config --global http.sslVerify false`

# Wie nütze ich dieses Repository?

Clonen Sie hierzu dieses Repository wie oben angegeben. Danach können Sie mit `git add`, `commit`, `push`, etc. damit arbeiten. Optimalerweise legen Sie hierzu nach dem initialen clone Ihren Namen (echten Namen, kein Nickname) und Ihre E-Mail-Adresse (E-Mail-Adresse der Universität Wien) fest sodass alle Commits Ihnen direkt zugeordnet werden können. Verwenden Sie hierzu folgende Befehle:

> `git config --global user.name "Mein Name"`

> `git config --global user.email a123456@univie.ac.at`

**Hilfe und Unterstützung für Git/GitLab**: Weitere Hilfen samt einer schrittweisen Einführung in den Umgang mit Git finden sich im Git & GitLab Screencast auf Moodle. Dort ist auch direkt ein Skriptum eingebunden um Details nachzulesen. Für erfahrene Studierende, ist als Referenz, bei den Screencasts auch ein Git Cheat-Sheet verlinkt. An einem der Übungstermine findet auch ein Git Tutorial statt. Anschließend können Sie immer auch unseren Tutor mit Fragen zu Git/GitLab, z.B. [email](mailto:tutor.swe1@univie.ac.at) oder (empfohlen) GitLab Issue kontaktieren. Unsere Tipps für GitLab Issues, gegen Ende dieses Dokumentes angeführt, bitte beachten. 

Für weiterführende Informationen lohnt sich ein Blick in das Pro Git Handbuch: https://git-scm.com/book/de/v2 Besonders für das Thema branching empfiehlt sich außerdem: https://learngitbranching.js.org/ 

# Welche Inhalte sind vorgegeben und wofür sind diese gedacht?

Es wurden mehrere **Ordner** sowie **.gitignore** Dateien vorgegeben. Letztere dienen dazu Ihr Repository nicht mit "unnötigen" Dateien zu befüllen, welche es erschweren würden Ihr Projekt während der Bewertung in die Entwicklungsumgebungen der Lektoren zu importieren (temporäre Dateien, etc.). Ändern Sie diese Dateien daher nicht bzw. nur sehr behutsam. Die Nutzung eigener **.gitignore** Dateien wird nicht empfohlen.

Die vorgegebenen Ordner sind wie folgt zu verwenden:
* **Dokumentation** - Nutzen Sie die darin enthaltenen Unterordner, pro Teilaufgabe ist ein anderer Unterordner vorgesehen, um Ihre _Dokumentation abzulegen_ bzw. abzugeben. 

   Dies ist bereits ab **Teilaufgabe 1** relevant, da Sie hier Ihre Ausarbeitung (das zu erstellende Markdown-Dokument, Dateiendung: `.md`) hinterlegen müssen, um diese abzugeben. Eine Vorlage sowie ein Markdown-Cheat-Sheet für die Ausarbeitung von **Teilaufgabe 1** finden Sie ebenfalls in diesem Ordner bzw. im Unterordner für **Teilaufgabe 1**. Zusätzlich müssen in dem für **Teilaufgabe 1** vorgesehenen Unterordner auch die **SVG-Dateien** (ein Vektorgrafikformat) abgelegt werden – jeweils für **Klassen- und Sequenzdiagramme**. Diese SVGs müssen auch direkt in das Markdown-Dokument eingebunden werden.  

   Für *Teilaufgabe 2* und *3* müssen Sie hier die verlangte Quellen-Dokumentation ebenfalls als Markdown-Dokument ablegen um deutlich zu machen welche Ideen und Konzepte basierend auf welchen Quellen erstellt wurden. Auch hierfür finden Sie in den jeweils vorgesehenen Unterordnern bereits passende Vorlagen. Quellen der *Teilaufgabe 1* direkt in das Ausarbeitungs-Markdown-Dokument einfügen. Bezüglich der Dateinamen und Pfade aller Abgaben die Angabe in Moodle beachten.

   Achten Sie darauf, dass die abgegebenen Inhalte **lesbar** sind und **korrekt** dargestellt werden. Können wir Ihre Inhalte nicht (sinnvoll) lesen kommt es zu Abzügen, dabei gilt:
   - Für die SVG-Dateien dient der in **Google Chrome** bzw. **Chromium** integrierte Renderer als Referenzimplementierung.  
   - Für die Markdown-Dateien dient der in diese **GitLab-Instanz** integrierte Parser als Referenzimplementierung.  
   - **Dateinamen und Pfade aus der Angabe auf Moodle beachten!** Dies ist immer wichtig, damit unsere Skripte Ihre Abgaben korrekt finden können.  

* **Executables** - Hinterlegen Sie hier, in den jeweils passenden Unterordnern, die finalen _kompilierten Abgaben Ihrer Implementierung_ für Teilaufgabe 2 und Teilaufgabe 3. Diese sollten .jar Dateien (bzw. pro Teilaufgabe muss es **genau eine einzelne** .jar Datei) seien, welche sich zumindest mit `java -jar <NameDerJarDatei.jar>` (plus die passenden Startargumente, siehe Moodle-Angabe) exekutieren lassen. **Prüfen Sie ob dies der Fall ist!** Genauere Informationen zu den zusätzlich anzuwendenden Parametern finden Sie auf **Moodle** in den Angaben und auf der **Evaluierungsplattform**. 

   Tipps dazu wie die notwendigen Jar Dateien erstellt werden können finden Sie ebenfalls in Moodle (in der Angabe zum nachlesen oder auch _vorgezeigt_ im _Eclipse/IDE Screencast_ auf Moodle). 
   
   Die Jar Dateien in dieses Repository zu übertragen dienen zu Ihrer **Sicherheit**, ist jedoch nicht zwingend notwendig. Sollte sich während der Beurteilung Ihr Projekt nicht automatisiert bauen lassen wird auf die hier hinterlegten Jar Dateien zurückgegriffen. Wenn dies notwendig wird kann es zu Punkteabzügen kommen. Legen Sie **genau bzw. maximal eine einzelne** .jar Datei pro Teilaufgabe ab. Sollten Sie mehrere .jar Dateien hinterlegen können unsere Skripte nicht entscheiden welche die passende ist.
   
   _Dieses Angebot stellt ein weiteres Sicherheitsnetz für Sie dar. Wir empfehlen es zu nützen. Testen Sie die Jar Dateien ausgiebig._ Wenn wir Ihre Abgabe weder selbst automatisch bauen können noch automatisch eine passende Backup Jar Datei auffinden können wird Ihre Abgabe unter Umständen mit 0 Punkten beurteilt. 

* **Source** - Nutzen Sie diesen Ordner, um die **Implementierung von Teilaufgabe 2 und Teilaufgabe 3** abzulegen (d. h. **Sourcecode, Konfigurationen** etc.).  Vorgehensweise, überblicksartig:

   1. Dieses **Repository clonen** und diesen Ordner als **Eclipse Workspace** wählen.  
   2. Das bereitgestellte **Beispielprojekt** (siehe Moodle-Angabe) in diesen Ordner bzw. den passenden Unterordner kopieren.  
      - **Wichtig:** Beachten Sie die in den Moodle-Angaben angegebenen **Ordner- und Pfadstrukturen**! Passen Sie das Beispielprojekt bzw. dieses Repository bei Bedarf entsprechend an.
   3. Die **README**-Anleitung im Beispielprojekt befolgen, um das Projekt in **Eclipse** zu importieren.  

   _Dieser Ablauf wird allgemein in den Screencasts zu **Eclipse/IDE** und **Git/GitLab** auf Moodle vorgezeigt._  

   Beachten Sie hinsichtlich der **zu verwendenden Pfade, Ordnernamen, Struktur und Projektbestandteile** die jeweilige Moodle-Angabe: **Die Vorgaben in den Angaben genau einzuhalten ist essenziell**, da sonst der **automatische Download, Bau und Upload zur Evaluierung durch die LV-Leitung fehlschlagen** könnte. Ist das der Fall hat dies eine Auswirkung auf die Bewertungsergbnisse, bis hin zu einer Beurteilung mit 0 Punkten.

# Was gilt es während der Implementierung zu beachten? 

* **Beispielprojekte**: Sehen Sie sich die auf Moodle bereitgestellten Beispielprojekte an (für _Teilaufgabe 2_ und _3_). Sie können diese mittels Eclipse einfach in den Eclipse-Workspace als Gradle-Projekt importieren und direkt mit der Implementierung beginnen. Oberhalb finden Sie eine Anleitung um die Beispielprojekte in Git bzw. GitLab zu integrieren.

* **Vor einer Deadline**: Während den Bewertungen wird Ihr Repository heruntergeladen (```git clone```). Danach wird der Inhalt des zur Teilaufgabe passenden Unterordners (Pfade, Dateinamen etc. in den Angaben auf Moodle beachten) bewertet. Für Teilaufgaben mit Implementierungen wird beispielsweise von den LV Leitern automatisiert diese Implementierung mittels Gradle gebaut und das so entstehende Jar automatisiert zur grundlegenden Funktionalitäts-Bewertung herangezogen. Anschließend wird, falls es für die jeweilige Teilaufgabe vorgesehen ist, noch Ihr Projekt in Eclipse importiert und dort genauer analysiert hinsichtlich nicht automatisch prüfbarer Inhalte (z.B. bezüglich besprochener Best Practices). 

   Prüfen Sie daher sicherheitshalber ob dies fehlerfrei möglich ist indem Sie dieses Repository neu klonen, neu in einen neuen Eclipse Workspace importieren und anschließen Ihre Projekte bauen sowie auch mittels Gradle in ein ausführbares Jar exportieren. Prüfen Sie anschließend dieses Jar mehrfach mit den bereitgestellten Lösungen zur Selbstevaluation. Bearbeiten Sie auftretende Probleme, prüfen Sie dann erneut, bearbeiten Probleme, usw. Eine Anleitung zum bauen der Jar's finden Sie in den Angaben der Teilaufgaben auf Moodle. 

* **Während der Bearbeitung**: Erstellen Sie keine zusätzlichen Ordner im Wurzelverzeichnis dieses Repositories und verändern Sie nicht die Namen, etc. der vorgegebenen Ordner. Zusätzliche Ordner, z.B. für Entwürfe, können Sie als Unterordner in den vorgegebenen Ordnern erstellen. Besser, bzw. mehr dem Stil von Git folgend, wäre es jedoch eigene *Branches* (siehe auch, branch-based development) zu nützen. Beachten Sie immer die in den Angaben auf Moodle vorgegebenen Namen und Pfade.

   Achten Sie darauf, dass sich die finale Abgabe an der in Moodle vorgegebenen Stelle befindet. Stellen Sie sicher, dass nicht mehrere unterschiedliche/widersprüchliche Versionen Ihrer Abgaben im `master`-Branch dieses Repositories enthalten sind. Es muss während der Bewertung schnell und einfach (bzw. sogar durch automatische Skripts) möglich sein zu erkennen wo sich Ihre Abgabe befindet und welche Inhalte für die jeweilige Teilaufgabe relevant sind. 

   Andernfalls kann eine Situation entstehen, bei welcher versehentlich, beispielsweise, eine veraltete Kopie zur Bewertung herangezogen wird und Sie deshalb nicht alle Punkte erhalten. Alternativ finden die Skripts Ihre Abgabe nicht oder nicht vollständig. Unter Umständen erhalten Sie dann keine Punkte. Eine spätere Korrektur und individuelle Nachfragen von unserer Seite sind Aufgrund der hohen Zahl an Studierenden organisatorisch nicht möglich.    

# Wie kann ich während der Implementierung Unterstützung erhalten?

Diese Lehrveranstaltung bietet auf vielen Ebenen Unterstützung an – wählen Sie nach Ihren Bedürfnissen. Beispielsweise wird für jedes für Sie neues Thema (z.B. Architektur, Netzwerk, Testing, Übungsangaben, usw.) ein Tutorial abgehalten, Beispielcode bereitgestellt und ein zugehöriges Skriptum auf Moodle hinterlegt. In den Vorbesprechungsfolien finden Sie eine Übersicht darüber, wann welches Thema behandelt wird. Gerne können Sie bei diesen Terminen auch zu themenfremden Bereichen Fragen stellen. Speziell dafür werden auch noch zusätzliche offene Fragestunden angeboten.

## Unterstützung außerhalb der Tutorials:

- Für **allgemeine Fragen**, von welchen Sie annehmen, dass diese für Sie und andere Studierende relevant sind empfehlen wir das jeweils passendste Moodle-Forum nützen. Dort können Sie auch mit anderen Studierenden diskutieren und sich gegenseitig helfen oder alte Fragen einsehen. Vielleicht findet sich genau Ihr Anliegen bereits dort.

- Für **spezifische Implementierungsfragen** empfehlen wir hier einen Git Issue zu erstellen. Beschreiben Sie darin Ihr Anliegen und - **wichtig** - vermerken Sie unseren Tutor. Hierzu dessen Git Handle im Issue Text, als Teil der Anfrage (inkl. @), einfügen. 

   Inhaltlich zielt diese Unterstützungsmöglichkeit darauf ab Sie bei für Sie **neuen** Themen in die Richtige Richtung zu lenken. Beispielsweise wenn die Netzwerkimplementierung nicht und nicht funktionieren will.

   Folgende Tutoren sind dieses Semester verfügbar: 

   - `Simon Eckerstorfer (Git Handle @simone99)`

   **Hilfe, niemand bearbeitet mein Git Issue**: Um den Tutor zu kontaktieren, fügen Sie bitte immer dessen Git-Handle im Beschreibungstext des Git Issues ein. Nur auf diese Weise wird die betreffende Person von GitLab über die Anfrage benachrichtigt. 

     - Weisen Sie ein Git-Issue *nicht* direkt zu, da nur durch das Einfügen eines Git-Handles im Beschreibungstext eine Benachrichtigung ausgelöst wird. 

     - Bitte verwenden Sie **niemals** `@all`, da dies alle Personen benachrichtigen würde, auch diejenigen, die nicht relevant sind. Tutoren leiten Anfragen bei Bedarf selbstständig an die passenden Personen weiter.

     - Bitte berücksichtigen Sie, dass Tutoren keine Fragen zur Beurteilung wie "Reicht das aus", "Wie viele Punkte erhalte ich", "muss ich das noch ändern", "darf ich das" etc. beantworten können da Tutoren die Beurteilung nicht durchführen. Für Fragen zur Bewertung siehe die Tutorials, Skripten und die dabei behandelten Best Practices sowie die Beurteilungskriterien in der Angabe.

- **Außerhalb von GitLab/Forum** können Sie uns per E-Mail erreichen. Den Tutor unter [Tutor-E-Mail](mailto:tutor.swe1@univie.ac.at) sowie die LV-Leitung unter [LV-Leitungs-E-Mail](mailto:swe1.wst@univie.ac.at). Immer an diese LV-spezifischen E-Mail-Adressen schreiben. Nie an persönliche Adressen. Bitte E-Mails jeweils nur an eine der E-Mail-Adressen schicken. Falls notwendig leiten die Tutoren Ihre Anfrage passend weiter.

# Welche Funktionen sollen nicht genutzt werden?

GitLab ist eine mächtige Software, die es erlaubt zahlreiche Einstellungen anzupassen. Wir würden dazu raten diese Möglichkeiten nicht unüberlegt zu nützen da unbedachte Aktionen (z.B. das Löschen des Masterbranches) hierbei auch negative Auswirkungen haben können da GitLab teilweise nicht nachfragt, sondern Aktionen einfach ausgeführt (*Think before you click!*). Verwenden Sie daher optimalerweise einfach die vorgegebenen Einstellungen. Zur Sicherheit wurden, soweit möglich, unnötige Funktionen von uns bereits deaktiviert.
