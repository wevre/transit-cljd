(ns build
  (:require [clojure.string :as str]
            [clojure.tools.build.api :as b]))

(defn- version []
  (let [ref (b/git-process {:git-args "describe --tags --match v0.8.0"})
        cnt (-> ref (str/split #"-") (get 1))]
    (format "0.8.%s" cnt)))

(defn- update-readme [ver]
  (let [rstr (format "s/release: [[:digit:]]\\{1,2\\}\\.[[:digit:]]\\{1,2\\}\\.[[:digit:]]\\{1,4\\}/release: %s/g" ver)]
    (b/process {:command-args ["sed" "-i" "" rstr "README.md"]})))

(defn- update-pubspec [ver]
  (let [rstr (format "s/version: [[:digit:]]\\{1,2\\}\\.[[:digit:]]\\{1,2\\}\\.[[:digit:]]\\{1,4\\}/version: %s/g" ver)]
    (b/process {:command-args ["sed" "-i" "" rstr "pubspec.yaml"]})))

(defn- update-changelog [ver notes]
  (let [notes (str/replace (str/trim notes) "\n" "\\\n")
        rstr (format "3i\\\n### %s\\\n\\\n%s\\\n\\\n" ver notes)]
    (println "ver is " ver)
    (println "notes are " notes)
    (b/process {:command-args ["sed" "-i" "" rstr "CHANGELOG.md"]})))

(defn release [{:keys [notes]}]
  (let [version (version)]
    (b/git-process {:git-args (format "tag -a v%1$s -m \"Release %1$s\"" version)})
    #_#_#_#_#_#_(b/git-process {:git-args "push"})
    (update-readme version)
    (update-pubspec version)
    (update-changelog version notes)
    (b/git-process {:git-args (format "commit -m \"update version references to %s\"" version)})
    (b/git-process {:git-args "push"})))

(comment
  (update-readme "0.8.5")
  (update-pubspec "0.8.5")
  (version)
  )