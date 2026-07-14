/**
 * Null-policy and precondition helpers for CardKit public API boundaries.
 *
 * <p>Convention summary:</p>
 * <ul>
 *   <li>Validate every public-API argument with {@link org.rezatron.cardkit.core.util.Require}
 *       at the top of the method body.</li>
 *   <li>Express "absent" results as {@link java.util.Optional} or empty collections,
 *       never as {@code null} returns.</li>
 * </ul>
 */
package org.rezatron.cardkit.core.util;

