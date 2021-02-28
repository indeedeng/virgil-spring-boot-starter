/*
 * Copyright 2014-2018 the original author or authors.
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

/* global SBA */
import custom from './custom';

const ROOT = 'instances';

//Instance Tab
// tag::customization-ui-toplevel[]
SBA.use({
  install({viewRegistry}) {
    viewRegistry.addView({
      name: 'virgil',  //Name of the view and the route to the view
      path: 'virgil', //Path where the view will be accessible
      parent: ROOT, //parent
      component: custom, //<3>
      label: 'Virgil', // Display name of the tab
      order: 1000,
      isEnabled: ({instance}) => {
        return instance.hasEndpoint('get-queues');
      }
    });
  }
});
// end::customization-ui-toplevel[]
