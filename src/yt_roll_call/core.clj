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
  (:import [javax.swing
            JFrame
            JLabel
            JTextField
            JButton
            JPanel
            JTextArea
            BoxLayout
            Box]
           [javax.swing.event CaretListener]
           [java.awt.event ActionListener]
           [java.awt BorderLayout Insets Dimension]))


(def messages-and-author-ids (atom [[] []]))
(def current-update-thread (atom nil))

(defn make-update-message-thread [video-id messages-and-author-ids-atom]
  (Thread. (fn []
             (cond
               (Thread/interrupted)
               nil
               :else
               (do (swap! messages-and-author-ids-atom
                          (fn [[messages author-ids]]
                            (message/new-messages (api/fetch-messages video-id)
                                                  messages
                                                  author-ids)))
                   (Thread/sleep 10000)
                   (recur))))))

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
                   [(subvec messages 1) author-ids]))))))

(defn ok-button-action [text-field
                        author-field
                        content-field
                        current-thread-atom
                        messages-and-author-ids-atom]
  (reify ActionListener
    (actionPerformed [_ evt]
      (let [button (.getSource evt)
            video-id (.getText text-field)]
        (and @current-update-thread (.interrupt @current-update-thread))
        (swap! current-thread-atom
               (fn [_]
                 (make-update-message-thread video-id
                                             messages-and-author-ids-atom)))
        (.setEnabled button false)
        (.setText author-field "Please")
        (.setText content-field "wait")
        (.start @current-thread-atom)
        (loop [counter 0]
          (cond
            (> counter 100)
            (do (.setText author-field "nil")
                (.setText content-field "nil")
                nil)
            (empty? (first @messages-and-author-ids-atom))
            (do (Thread/sleep 200)
                (recur (inc counter)))
            :else
            (display-message author-field
                             content-field
                             messages-and-author-ids-atom)))))))


(def main-frame (JFrame. "點點名"))
(def video-id-label (JLabel. "Video ID: "))
(def video-id-input (JTextField.))
(def video-id-button (JButton. "ok"))
(def message-author (JLabel. "nil" javax.swing.SwingConstants/RIGHT))
(def message-content (JLabel. "nil"))
(def next-button (JButton. "next"))
(def info-button (JButton. "info"))

(doto main-frame
  (.setSize 500 200)
  (.setLayout nil)
  (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
  (.setResizable false)
  (.add video-id-label)
  (.add video-id-input)
  (.add video-id-button)
  (.add message-author)
  (.add message-content)
  (.add next-button)
  (.add info-button))

(.setBounds video-id-label 10 10 80 30)
(.setBounds video-id-input 100 10 250 30)
(.setBounds video-id-button 360 10 80 30)
(.addActionListener video-id-button (ok-button-action video-id-input
                                                      message-author
                                                      message-content
                                                      current-update-thread
                                                      messages-and-author-ids))

(.setBounds message-author 10 60 150 30)
(.setBounds message-content 170 60 300 30)

(.setBounds next-button 200 110 100 30)
(.addActionListener next-button (reify ActionListener
                                  (actionPerformed [_ evt]
                                    (display-message message-author
                                                     message-content
                                                     messages-and-author-ids))))

;; The ActionListener of this button is defined in the info section.
(.setBounds info-button 200 160 100 30)


(def not-found-frame (JFrame. "File Not Found"))
(def not-found-label (JLabel. "Token file ('token') not found."))

(doto not-found-frame
  (.setSize 300 200)
  (.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE)
  (.add not-found-label))


(def info-frame (JFrame. "Info"))
(def info-text (JTextArea. "yt-roll-call  Copyright (C) 2023  CToID

This program comes with ABSOLUTELY NO WARRANTY.

This is free software, and you are welcome to redistribute it under certain conditions. See the GNU General Public License in <https://www.gnu.org/licenses>, for more details.

The source code of this program is hosted at <https://www.github.com/funk443/yt-roll-call>."))

(doto info-frame
  (.setSize 500 200)
  (.setDefaultCloseOperation JFrame/HIDE_ON_CLOSE)
  (.add info-text))

(doto info-text
  (.setMargin (Insets. 5 5 5 5))
  (.setLineWrap true)
  (.setWrapStyleWord true)
  (.setEditable false))

(.addActionListener info-button (reify ActionListener
                                  (actionPerformed [_ evt]
                                    (.setVisible info-frame true))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (if (.exists (clojure.java.io/file "token"))
    (.setVisible main-frame true)
    (.setVisible not-found-frame true)))
