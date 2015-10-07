/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License, Version 1.0 only
 * (the "License").  You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the license at legal-notices/CDDLv1_0.txt
 * or http://forgerock.org/license/CDDLv1.0.html.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at legal-notices/CDDLv1_0.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *      Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 *
 *
 *      Copyright 2009-2010 Sun Microsystems, Inc.
 *      Portions Copyright 2014-2015 ForgeRock AS
 */
package org.opends.server.backends.pluggable;

import org.forgerock.i18n.LocalizableMessageBuilder;

/** This interface represents a Backend Query. */
// @FunctionalInterface
interface IndexQuery
{
  /**
   * Evaluates the index query and returns the EntryIDSet.
   *
   * @param debugMessage If not null, diagnostic message will be written
   *                      which will help to determine why the returned
   *                      EntryIDSet is not defined.
   * @param indexNameOut If not null, output parameter for the name of the index type actually used to return
   *                      index results.
   * @return The non null EntryIDSet as a result of evaluating this query
   */
  EntryIDSet evaluate(LocalizableMessageBuilder debugMessage, StringBuilder indexNameOut);
}
