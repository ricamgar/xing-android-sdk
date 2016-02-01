/*
 * Copyright (С) 2015 XING AG (http://xing.com/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xing.api.resources;

import com.xing.api.CallSpec;
import com.xing.api.HttpError;
import com.xing.api.Resource;
import com.xing.api.XingApi;
import com.xing.api.data.events.Event;
import com.xing.api.internal.Experimental;

/**
 * @author daniel.hartwich
 */
public class EventsResource extends Resource {
    /** Creates a resource instance. This should be the only constructor declared by child classes. */
    EventsResource(XingApi api) {
        super(api);
    }

    /**
     * Event Details
     */
    @Experimental
    public CallSpec<Event, HttpError> getEventById(String eventId) {
        return Resource.<Event, HttpError>newGetSpec(api, "v1/events/{id}")
              .pathParam("id", eventId)
              .responseAs(single(Event.class, "event"))
              .build();
    }


}
