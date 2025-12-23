package client.view;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import client.model.common.Notification;
import client.model.mapper.HalfMapDimensions;
import client.model.mapper.MapNode;
import client.model.mapper.validator.MapValidator;
import client.model.mapper.PlayerHalfMap;
import client.model.mapper.Terrain;

class MapValidationInternalsViewTest {

	@Test
	void report_includes_reference_for_invalid_map() {
		// Arrange: force a validation failure ("negative" path)
		Notification notification = new MapValidator().validate(null);
		assertTrue(notification.hasErrors(), "Precondition: validation should have produced errors");

		ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream();
		PrintStream originalErr = System.err;

		try {
			System.setErr(new PrintStream(stderrBuffer, true, StandardCharsets.UTF_8));

			// Act
			new MapValidationInternalsView().report(notification);
		} finally {
			System.setErr(originalErr);
		}

		String stderr = stderrBuffer.toString(StandardCharsets.UTF_8);

		// Assert: should contain an error type, details, and a class+method reference.
		assertTrue(stderr.contains("Type:"), "Expected 'Type:' marker in System.err");
		assertTrue(stderr.contains("Details:"), "Expected 'Details:' marker in System.err");
		assertTrue(stderr.contains("Kind:"), "Expected 'Kind:' marker in System.err");
		assertTrue(stderr.contains("Message:"), "Expected 'Message:' marker in System.err");
		assertTrue(stderr.contains("Reference:"), "Expected 'Reference:' marker in System.err");
		assertTrue(stderr.contains("at "), "Expected stacktrace-like 'at ...' reference in System.err");

		// Reference extraction is stack-trace based; make sure we got a validator-ish frame.
		assertTrue(stderr.contains("Validator") || stderr.contains("validate("),
				"Expected validator reference (class/method) in System.err output");

		System.out.println("---- Captured System.err ----");
		System.out.println(stderr);
	}

	@Test
	void report_prints_validator_class_and_method_for_real_rule_violation() {
		// Arrange: build a structurally valid half-map, but violate terrain distribution + fort rules.
		PlayerHalfMap halfMap = new PlayerHalfMap("p1");
		for (int y = 0; y < HalfMapDimensions.HEIGHT; y++) {
			for (int x = 0; x < HalfMapDimensions.WIDTH; x++) {
				// All grass, no fort -> triggers: insufficient mountains/water + incorrect fort count.
				halfMap.addMapNode(new MapNode(x, y, Terrain.GRASS, false, false));
			}
		}

		Notification notification = new MapValidator().validate(halfMap);
		assertTrue(notification.hasErrors(), "Precondition: validation should have produced errors");

		ByteArrayOutputStream stderrBuffer = new ByteArrayOutputStream();
		PrintStream originalErr = System.err;
		try {
			System.setErr(new PrintStream(stderrBuffer, true, StandardCharsets.UTF_8));
			new MapValidationInternalsView().report(notification);
		} finally {
			System.setErr(originalErr);
		}

		String stderr = stderrBuffer.toString(StandardCharsets.UTF_8);

		// Print for manual review (what you asked for).
		System.out.println("---- Captured System.err (real rule violation) ----");
		System.out.println(stderr);

		// Minimal automated checks that match the task text structure.
		assertTrue(stderr.contains("Type:"), "Expected 'Type:' marker in System.err");
		assertTrue(stderr.contains("Details:"), "Expected 'Details:' marker in System.err");
		assertTrue(stderr.contains("Kind:"), "Expected 'Kind:' marker in System.err");
		assertTrue(stderr.contains("Message:"), "Expected 'Message:' marker in System.err");
		assertTrue(stderr.contains("Reference:"), "Expected 'Reference:' marker in System.err");
		assertTrue(stderr.contains("at "), "Expected stacktrace-like 'at ...' reference in System.err");

		// Ensure a validator method/class is referenced (ideally the rule validator, not just MapValidator).
		assertTrue(stderr.contains("Validator"), "Expected a validator class to appear in Reference");
		assertTrue(stderr.contains("validate"), "Expected a validation method name to appear in Reference");
	}
}
