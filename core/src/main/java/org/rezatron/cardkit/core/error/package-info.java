/**
 * Domain exception hierarchy for CardKit rule and programming violations.
 *
 * <p>All exceptions in this package are <em>unchecked</em> (extend {@link
 * org.rezatron.cardkit.core.error.CardKitException}). They are thrown when a caller
 * violates a domain rule (e.g. dealing from an exhausted source, acting out of phase)
 * or constructs a type incorrectly (e.g. an invalid hand). Game outcomes (win/lose/push,
 * hi-lo qualification) are modelled as return values — never as exceptions.</p>
 *
 * <h2>Null policy</h2>
 * <p>Public API boundaries reject {@code null} arguments immediately via
 * {@link java.util.Objects#requireNonNull}. "Absent" results are expressed with
 * {@link java.util.Optional} or empty collections, never {@code null}.</p>
 *
 * <h2>Logging convention</h2>
 * <p>Every class that needs logging declares exactly one SLF4J logger:</p>
 * <pre>{@code
 *   private static final Logger log = LoggerFactory.getLogger(MyClass.class);
 * }</pre>
 * <p>This library binds <strong>no</strong> logging implementation; the consumer supplies
 * one. Internal detail is logged at {@code DEBUG} / {@code TRACE} only — no {@code INFO}
 * chatter during normal operation.</p>
 */
package org.rezatron.cardkit.core.error;

