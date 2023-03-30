(ns build
  (:require [clojure.string :as str]
            [clojure.tools.build.api :as b]))

(defn- version []
  (let [ref (b/git-process {:git-args "describe --tags --match v0.8.0"})
        cnt (-> ref (str/split #"-") (get 1))]
    (format "0.8.%s" cnt)))

(defn sha [ver]
  (b/git-process {:git-args ["rev-parse" "--short" (format "v%s^{commit}" ver)]}))

(defn- update-readme [ver]
  (let [ver-str (format "s/release: [[:digit:]]\\{1,2\\}\\.[[:digit:]]\\{1,2\\}\\.[[:digit:]]\\{1,4\\}/release: %s/g" ver)
        sha (b/git-process {:git-args ["rev-parse" "--short" (format "v%s^{commit}" ver)]})
        sha-str (format "s/sha \"[[:xdigit:]]\\{7,\\}\"/sha \"%s\"/g" sha)]
    (b/process {:command-args ["sed" "-i" "" "-e" ver-str "-e" sha-str "README.md"]})))

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
    (b/git-process {:git-args ["tag" "-a" (format "v%s" version) "-m" (format "Release %s" version)]})
    (b/git-process {:git-args "push"})
    (update-readme version)
    (update-pubspec version)
    (update-changelog version notes)
    (b/git-process {:git-args ["commit" "-m" (format "update doc refs to version %s" version)]})
    (b/git-process {:git-args "push"})))

(comment
  (update-readme "0.8.6")
  (update-pubspec "0.8.5")
  (version)
  (sha "0.8.6")

  )