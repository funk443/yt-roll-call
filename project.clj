;;;; yt-roll-call: a small tool to conveniently say hi to everyone in yt chat.
;;;; Copyright (C) 2023  CToID
;;;;
;;;; This file is part of yt-roll-call.
;;;;
;;;; yt-roll-call is free software: you can redistribute it and/or modify it
;;;; under the terms of the GNU General Public License as published by the Free
;;;; Software Foundation, either version 3 of the License, or (at your option)
;;;; any later version.
;;;;
;;;; yt-roll-call is distributed in the hope that it will be useful, but WITHOUT
;;;; ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
;;;; FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
;;;; more details.
;;;;
;;;; You should have received a copy of the GNU General Public License along
;;;; with yt-roll-call. If not, see <https://www.gnu.org/licenses/>.


(defproject yt-roll-call "0.1"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "GPL-3.0-or-later"
            :url "https://www.gnu.org/licenses"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [clj-http "3.12.3"]
                 [cheshire "5.12.0"]]
  :main ^:skip-aot yt-roll-call.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
