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


(ns yt-roll-call.core
  (:gen-class)
  (:require [yt-roll-call.api :as api]
            [yt-roll-call.message :as message])
  (:import [javax.swing JFrame JLabel JTextField JButton JPanel]
           [java.awt.event ActionListener]
           [java.awt GridBagLayout GridBagConstraints Insets Dimension]))


(def messages-and-author-ids (atom [[] []]))
(def current-update-thread (atom nil))

(defn make-update-message-thread [video-id messages-and-author-ids-atom]
  (Thread. (fn []
             (swap! messages-and-author-ids-atom
                    (fn [[messages author-ids]]
                      (message/new-messages (api/fetch-messages video-id)
                                            messages
                                            author-ids)))
             (Thread/sleep 10000)
             (recur))))

(defn display-message [author-label
                       content-label
                       messages-and-author-id-atoms]
  (let [[messages author-ids] @messages-and-author-id-atoms
        message (first messages)]
    (cond
      (nil? message)
      (do (.setText author-label "nil")
          (.setText content-label "nil"))
      :else
      (do (.setText author-label (str (:author-name message) ": "))
          (.setText content-label (:content message))
          (swap! messages-and-author-id-atoms
                 (fn [_]
                   [(vec (rest messages)) author-ids]))))))

(defn ok-button-action [text-field
                        author-field
                        content-field
                        current-thread-atom
                        messages-and-author-ids-atom]
  (reify ActionListener
    (actionPerformed [_ evt]
      (let [button (.getSource evt)
            video-id (.getText text-field)]
        (swap! current-thread-atom
               (fn [_]
                 (make-update-message-thread video-id
                                             messages-and-author-ids-atom)))
        (.start @current-thread-atom)
        (.setEnabled button false)
        (Thread/sleep 2000)
        (display-message author-field
                         content-field
                         messages-and-author-ids-atom)))))


(def main-frame (JFrame. "Hello"))
(def grid-bag-layout (GridBagLayout.))
(def grid-bag-constraint (GridBagConstraints.))
(def video-id-input (JTextField. "Video ID Here"))
(def video-id-button (JButton. "ok"))
(def message-author (JLabel. "nil"))
(def message-content (JLabel. "nil"))
(def next-button (JButton. "next"))

(.setLayout main-frame grid-bag-layout)
(.setSize main-frame 500 500)
(.setDefaultCloseOperation main-frame JFrame/EXIT_ON_CLOSE)

(set! (. grid-bag-constraint fill) GridBagConstraints/HORIZONTAL)
(set! (. grid-bag-constraint insets) (Insets. 10 5 10 5))
(set! (. grid-bag-constraint weightx) 0.2)
(set! (. grid-bag-constraint weighty) 0.2)

(set! (. grid-bag-constraint gridwidth) 2)
(set! (. grid-bag-constraint gridx) 0)
(set! (. grid-bag-constraint gridy) 0)
(.add main-frame video-id-input grid-bag-constraint)
(.setPreferredSize video-id-input (Dimension. 200 30))

(set! (. grid-bag-constraint gridwidth) 1)
(set! (. grid-bag-constraint gridx) 2)
(set! (. grid-bag-constraint gridy) 0)
(.add main-frame video-id-button grid-bag-constraint)
(.setPreferredSize video-id-button (Dimension. 50 30))
(.addActionListener video-id-button (ok-button-action video-id-input
                                                      message-author
                                                      message-content
                                                      current-update-thread
                                                      messages-and-author-ids))

(set! (. grid-bag-constraint gridwidth) 1)
(set! (. grid-bag-constraint gridx) 0)
(set! (. grid-bag-constraint gridy) 1)
(.add main-frame message-author grid-bag-constraint)
(.setPreferredSize message-author (Dimension. 150 30))

(set! (. grid-bag-constraint gridwidth) 2)
(set! (. grid-bag-constraint gridx) 1)
(set! (. grid-bag-constraint gridy) 1)
(.add main-frame message-content grid-bag-constraint)
(.setPreferredSize message-content (Dimension. 300 30))

(set! (. grid-bag-constraint gridwidth) 1)
(set! (. grid-bag-constraint gridx) 1)
(set! (. grid-bag-constraint gridy) 2)
(.add main-frame next-button grid-bag-constraint)
(.addActionListener next-button (reify ActionListener
                                  (actionPerformed [_ evt]
                                    (display-message message-author
                                                     message-content
                                                     messages-and-author-ids))))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (.setVisible main-frame true))
