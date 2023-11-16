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


(ns yt-roll-call.api
  (:require [clj-http.client :as http]
            [cheshire.core :as json]))


(def yt-get
  (let [api-base-url "https://www.googleapis.com/youtube/v3"
        api-key (and (.exists (clojure.java.io/file "token"))
                     (.trim (slurp "token")))]
    (fn [endpoint q-string]
      (-> (.concat api-base-url endpoint)
          (http/get {:accept :json
                     :query-params (conj q-string {"key" api-key})})
          :body
          json/parse-string))))

(defn fetch-messages
  ([video-id] (fetch-messages video-id 100))
  ([video-id max-results]
   (let [live-chat-id
         (get-in (yt-get "/videos" {"part" "liveStreamingDetails"
                                    "id" video-id
                                    "maxResults" max-results})
                 ["items" 0 "liveStreamingDetails" "activeLiveChatId"])]
     (get (yt-get "/liveChat/messages" {"liveChatId" live-chat-id
                                        "part" "snippet,authorDetails"
                                        "maxResults" max-results})
          "items"))))
