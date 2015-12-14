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

package com.xing.android.sdk.resources;

import com.xing.android.sdk.CallSpec;
import com.xing.android.sdk.Resource;
import com.xing.android.sdk.XingApi;
import com.xing.android.sdk.model.Recommendation;
import com.xing.android.sdk.model.user.XingUser;

/**
 * @author daniel.hartwich
 */
public class RecommendationsResource extends Resource {
    /**
     * Creates a resource instance. This should be the only constructor declared by child classes.
     */
    protected RecommendationsResource(XingApi api) {
        super(api);
    }

    /**
     * Get recommendations.
     *
     * Returns a list of users the specified user might know.
     *
     * <table>
     * <h4>Possible OPTIONAL parameters</h4>
     * <tr>
     * <th>Paramter Name</th>
     * <th><b>Description</b></th>
     * </tr>
     * <tr>
     * <td><b>limit</b></td>
     * <td>Limit the number of recommendations to be returned. This must be a positive number. Default: 10, Maximum:
     * 100.</td>
     * </tr>
     * <tr>
     * <td><b>offset</b></td>
     * <td>Offset. This must be a positive number. Default: 0</td>
     * </tr>
     * <tr>
     * <td><b>user_fields</b></td>
     * <td>List of user attributes to return. If this parameter is not used, only the ID will be returned.
     * For a list of available profile user attributes, please refer to the get user details call.
     * {@link XingUser}</td>
     * </tr>
     * </table>
     *
     * @param userId ID of the user the recommendations are generated for.
     */
    public CallSpec<Recommendation, Object> getRecommendations(String userId) {
        return Resource.<Recommendation, Object>newGetSpec(api, "/v1/users/{user_id}/network/recommendations")
              .pathParam("user_id", userId)
              .responseAs(Recommendation.class, "user_recommendations")
              .build();
    }

    /**
     * Block recommendations.
     *
     * Block recommendation for user with given id. This will block all future occurances of this recomendation for
     * the given User ID.
     *
     * @param userId User ID
     * @param id User ID which should not appear in further recommendations
     */
    public CallSpec<String, String> blockRecomendation(String userId, String id) {
        return Resource.<String, String>newDeleteSpec(api, "/v1/users/{user_id}/network/recommendations/user/{id}")
              .pathParam("user_id", userId)
              .pathParam("id", id)
              .responseAs(String.class)
              .build();
    }
}
