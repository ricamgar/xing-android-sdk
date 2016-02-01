/*
 * Copyright (c) 2015 XING AG (http://xing.com/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.xing.api.data.events;

import com.squareup.moshi.Json;

import java.io.Serializable;
import java.util.List;

/**
 * @author daniel.hartwich
 */
public class Rsvp implements Serializable {
    private static final long serialVersionUID = -7066641500126545086L;

    @Json(name = "selected")
    private final String selected;
    @Json(name = "possible_actions")
    private final List<String> possibleActions;
    @Json(name = "purchase_url")
    private final String purchaseUrl;

    public Rsvp(String selected, List<String> possibleActions, String purchaseUrl) {
        this.selected = selected;
        this.possibleActions = possibleActions;
        this.purchaseUrl = purchaseUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Rsvp rsvp = (Rsvp) o;

        return (selected != null ? selected.equals(rsvp.selected) : rsvp.selected == null) && (possibleActions != null
              ? possibleActions.equals(rsvp.possibleActions) : rsvp.possibleActions == null);
    }

    @Override
    public int hashCode() {
        int result = selected != null ? selected.hashCode() : 0;
        result = 31 * result + (possibleActions != null ? possibleActions.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Rsvp{" +
              "selected='" + selected + '\'' +
              ", possibleActions=" + possibleActions +
              '}';
    }

    public String selected() {
        return selected;
    }

    public List<String> possibleActions() {
        return possibleActions;
    }
}
