;;;;   Copyright 2016 Peter Stephens. All Rights Reserved.
;;;;
;;;;   Licensed under the Apache License, Version 2.0 (the "License");
;;;;   you may not use this file except in compliance with the License.
;;;;   You may obtain a copy of the License at
;;;;
;;;;       http://www.apache.org/licenses/LICENSE-2.0
;;;;
;;;;   Unless required by applicable law or agreed to in writing, software
;;;;   distributed under the License is distributed on an "AS IS" BASIS,
;;;;   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;;;;   See the License for the specific language governing permissions and
;;;;   limitations under the License.

(ns biblecli.commands.verseofthedaygadget
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require
    [clojure.string :as s]
    [common.normalizer.core :refer [parse]]
    [hiccups.runtime :as hiccupsrt]))

(def node-fs (js/require "fs"))
(def node-path (js/require "path"))

(defn gadget-html [m]
  "<div>Content Goes Here</div>")

(defn gadget [m]
  (str "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>
<Module>
  <ModulePrefs title=\"Verse of the Day\"
               title_url=\"https://kingjames.bible\"
               description=\"Daily verses from the King James Bible.\"
               author=\"Peter Stephens\"
               author_email=\"feedback@kingjames.bible\"
               thumbnail=\"https://kingjames.bible/votd/votd_thumb.png\"
               height=\"200\" />
  <Content type=\"html\">
  <![CDATA[
  <!DOCTYPE html>
" (gadget-html m) "
  ]]>
  </Content>
</Module>"))

(defn write! [dir filename content]
  (let [filepath (.join node-path dir filename)
        buff (js/Buffer content "utf8")]
    (.writeFileSync node-fs filepath buff)))

(defn prepare! [parser src config output-dir]
  (let [m (parse parser src)]
    (write! output-dir "votd.xml" (gadget m))))