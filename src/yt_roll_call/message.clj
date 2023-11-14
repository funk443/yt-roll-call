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


(ns yt-roll-call.message)


(defn parse-message [raw-message]
  {:author-id (get-in raw-message ["authorDetails" "channelId"])
   :author-name (get-in raw-message ["authorDetails" "displayName"])
   :content (get-in raw-message ["snippet" "displayMessage"])})

(defn new-messages [[raw-message & more :as raw-messages]
                    old-messages
                    author-ids]
  (let [message (parse-message raw-message)]
    (cond
      (empty? raw-messages)
      [old-messages author-ids]
      (some (partial = (:author-id message)) author-ids)
      (recur more old-messages author-ids)
      :else
      (recur more
             (conj old-messages message)
             (conj author-ids (:author-id message))))))
