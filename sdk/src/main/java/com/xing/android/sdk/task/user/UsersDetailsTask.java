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
package com.xing.android.sdk.task.user;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.xing.android.sdk.json.user.XingUserMapper;
import com.xing.android.sdk.model.user.XingUser;
import com.xing.android.sdk.model.user.field.XingUserField;
import com.xing.android.sdk.network.request.UserProfilesRequests;
import com.xing.android.sdk.task.OnTaskFinishedListener;
import com.xing.android.sdk.task.PrioritizedRunnable.Priority;
import com.xing.android.sdk.task.Task;

import java.util.List;

/**
 * @author daniel.hartwich
 * @author david.gonzalez
 * @see UserProfilesRequests#details(List, List)
 */
@SuppressWarnings("unused")
public class UsersDetailsTask extends Task<List<XingUser>> {
    private final List<String> mUserIds;
    private final List<XingUserField> mFields;

    /**
     * @param userIds  Ids of the users whose details are being returned
     * @param fields   The requested fields for the user objects to be returned
     * @param tag      Object that allows the task manager to cancel or stop listening ths task.
     * @param listener Observer that will be notified with the de-serialized result in case of success, or with an exception in case of failure.
     */
    public UsersDetailsTask(@Nullable List<String> userIds,
                            @Nullable List<XingUserField> fields,
                            @NonNull Object tag,
                            @NonNull OnTaskFinishedListener<List<XingUser>> listener) {
        this(userIds, fields, tag, listener, null);
    }

    /**
     * @param userIds  Ids of the users whose details are being returned
     * @param fields   The requested fields for the user objects to be returned
     * @param tag      Object that allows the task manager to cancel or stop listening ths task.
     * @param listener Observer that will be notified with the de-serialized result in case of success, or with an exception in case of failure.
     * @param priority Determines the position of the task on the queue of execution. It is a value of {@link Priority Priority}
     */
    public UsersDetailsTask(@Nullable List<String> userIds,
                            @Nullable List<XingUserField> fields,
                            @NonNull Object tag,
                            @NonNull OnTaskFinishedListener<List<XingUser>> listener,
                            @Nullable Priority priority) {
        super(tag, listener, priority);
        mUserIds = userIds;
        mFields = fields;
    }

    /**
     * Executes the {@link UserProfilesRequests#details(List, List)} request and deserialize the result.
     *
     * @return List of XingUser objects with the information of the users with the ids received on the constructor.
     * @throws Exception Can be {@link com.xing.android.sdk.network.oauth.OauthSigner.XingOauthException XingOauthException},
     *                   {@link com.xing.android.sdk.network.request.exception.NetworkException NetworkException}
     *                   or {@link com.xing.android.sdk.json.XingJsonException XingJsonException}.
     */
    @Override
    public List<XingUser> run() throws Exception {
        String response = UserProfilesRequests.details(mUserIds, mFields);
        return XingUserMapper.parseDetailsResponse(response);
    }
}
