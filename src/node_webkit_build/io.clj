(ns node-webkit-build.io
  (:import (java.io File FileOutputStream FileInputStream)
           (org.apache.commons.io.input CountingInputStream))
  (:require [clj-http.client :as http]
            [fs.core :as fs]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh with-sh-dir]]
            [node-webkit-build.util :refer [insert-after print-progress-bar]]))

;; aliases
(def reader #'io/reader)
(def file #'io/file)
(def make-parents #'io/make-parents)
(def input-stream #'io/input-stream)
(def output-stream #'io/output-stream)

(def file? #'fs/file?)
(def base-name #'fs/base-name)
(def exists? #'fs/exists?)
(def mkdirs #'fs/mkdirs)

(defn path-join [& parts] (clojure.string/join (File/separator) parts))

(defn path-files [path]
  (->> (file-seq (file path))
       (filter fs/file?)))

(defn wrap-downloaded-bytes-counter
  "Middleware that provides an CountingInputStream wrapping the stream output"
  [client]
  (fn [req]
    (let [resp (client req)
          counter (CountingInputStream. (:body resp))]
      (merge resp {:body counter
                   :downloaded-bytes-counter counter}))))

(defn download-with-progress [url target]
  (http/with-middleware
    (-> http/default-middleware
        (insert-after http/wrap-redirects wrap-downloaded-bytes-counter)
        (conj http/wrap-lower-case-headers))
    (let [request (http/get url {:as :stream})
          length (Integer. (get-in request [:headers "content-length"] 0))
          buffer-size (* 1024 10)]
      (with-open [input (:body request)
                  output (io/output-stream target)]
        (let [buffer (make-array Byte/TYPE buffer-size)
              counter (:downloaded-bytes-counter request)]
          (loop []
            (let [size (.read input buffer)]
              (when (pos? size)
                (.write output buffer 0 size)
                (print "\r")
                (print-progress-bar (.getByteCount counter) length)
                (recur))))))
      (println))))

(defn copy [input output]
  (sh "cp" "-r" input output))

(defn zip [source-path target-path]
  (let [source-path (fs/absolute-path source-path)
        target-path (fs/absolute-path target-path)]
    (with-sh-dir source-path
      (apply sh "zip" "-r" target-path (fs/list-dir source-path))))
  target-path)

(defn unzip [zip-path target-path]
  (sh "unzip" "-q" zip-path "-d" target-path))

(defn relative-path [source target]
  (let [source (fs/absolute-path source)
        target (fs/absolute-path target)]
    (.substring target (-> source count inc))))
