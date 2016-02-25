/*
 * Copyright 2016 Jens Walter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.trivium.extension.binding.webui;

import com.sun.net.httpserver.HttpHandler;
import io.trivium.extension.binding.Binding;
import io.trivium.glue.Http;
import io.trivium.glue.binding.http.StaticResourceHandler;

public class WebUI extends Binding {
    HttpHandler resHandler = new StaticResourceHandler("/ui/res/", "io.trivium.extension.binding.webui.res");
    WebUIHandler webUIHandler = new WebUIHandler();

    @Override
    protected void start() {
        Http.registerListener("/ui/", webUIHandler);
        Http.registerListener("/ui/res/", resHandler);
    }

    @Override
    protected void stop() {
        Http.unregisterListener(resHandler);
        Http.unregisterListener(webUIHandler);
    }

}
