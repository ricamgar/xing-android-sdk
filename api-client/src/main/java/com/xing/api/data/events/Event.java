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
import com.xing.api.data.Location;
import com.xing.api.data.profile.XingUser;

import java.io.Serializable;

/**
 * @author daniel.hartwich
 */
public class Event implements Serializable {
    private static final long serialVersionUID = 3700411617909052580L;

    @Json(name = "id")
    private String id;
    @Json(name = "title")
    private String title;
    @Json(name = "short_description")
    private String shortDescription;
    @Json(name = "description")
    private String description;
    @Json(name = "language")
    private String language;
    @Json(name = "online")
    private boolean online;
    @Json(name = "location")
    private Location location;
    @Json(name = "creator")
    private XingUser creator;
    //TODO: Find a way to implement organizer
    //    @Json(name = "organizer")
    //    private Organizer organizer;
    @Json(name = "participants")
    private Participants participants;


}
