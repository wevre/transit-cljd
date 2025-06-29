(ns build
  (:require [clojure.string :as str]
            [clojure.tools.build.api :as b]))

(def version-marker #"--TRANSIT_CLJD_VERSION--")
(def deps-marker #"--TRANSIT_CLJD_DEPS--")
(def version-pattern #"0\.8\.\d{1,4}")

;; Helper functions

(defn version
  "Runs git command `describe` to find the number of commits since the earliest,
   which is tagged with 'v0.8.0'."
  []
  (let [ref (b/git-process {:git-args "describe --tags --match v0.8.0"})
        cnt (-> ref (str/split #"-") (get 1))]
    (format "0.8.%s" cnt)))

(defn sha
  "Returns the commit sha for the commit with tag 'v<ver>'."
  [ver]
  (b/git-process {:git-args ["rev-parse" "--short" (format "v%s^{commit}" ver)]}))

(defn update-version
  "Finds lines containing `marker` and replaces version number."
  [lines ver & {:keys [pattern marker]
                                   :or {pattern version-pattern
                                        marker version-marker}}]
  (map #(if (re-find marker %) (str/replace % pattern ver) %) lines))

(defn update-deps-entry
  "Splits lines at marker and then replaces the four lines after the split with
   updated deps.edn git coordinates."
  [lines ver & {:keys [marker] :or {marker deps-marker}}]
  (let [sha (sha ver)
        [bef aft] (split-with #(not (re-find marker %)) lines)
        [ss rr] (split-at 4 aft)
        ss (assoc (vec ss) 2 (format "io.github.wevre/transit-cljd {:git/tag \"v%s\" :git/sha \"%s\"}" ver sha))]
    (concat bef ss rr)))

(defn add-build-notes
  "Splits lines after title line, inserts section for new version and notes."
  [lines ver notes]
  (let [[bef aft] (split-at 1 lines)
        hdr ["" (str "### " ver) ""]]
    (concat bef hdr (map #(str "- " %) notes) aft)))

;; Update README.md

(defn update-readme
  "Updates the version and the deps.edn git coordinates in `README.md`."
  [ver & {:keys [filename] :or {filename "README.md"}}]
  (->> (-> (slurp filename)
           str/split-lines
           (update-version ver)
           (update-deps-entry ver))
       (str/join "\n")
       (spit filename)))

(comment
  (update-readme "0.8.12" :filename "TEST_README.md")
  )

;; Update pubspec.yaml

(defn update-pubspec
  "Updates the version in `pubspec.yaml`."
  [ver & {:keys [filename] :or {filename "pubspec.yaml"}}]
  (->> (-> (slurp filename)
           str/split-lines
           (update-version ver))
       (str/join "\n")
       (spit filename)))

(comment
  (update-pubspec "0.8.12" :filename "pubspec copy.yaml")
  )

;; Update CHANGELOG.md

(defn update-changelog
  "Add an entry for newest version, with notes, in `CHANGELOG.md`."
  [ver notes & {:keys [filename] :or {filename "CHANGELOG.md"}}]
  (->> (-> (slurp filename)
           str/split-lines
           (add-build-notes ver notes))
       (str/join "\n")
       (spit filename)))

(comment
  (update-changelog "0.8.13" ["first note" "second note"] :filename "CHANGELOG copy.md"))

;; Build target: 'release'

#_{:clojure-lsp/ignore [:clojure-lsp/unused-public-var]}
(defn release
  "Adds a tag to the latest commit and pushes that, then updates version
   references in README.md, pubspec.yaml, and CHANGELOG.md and pushes those."
  [{:keys [notes]}]
  (let [ver (version)
        tag (format "v%s" ver)]
    (b/git-process {:git-args ["tag" "-a" tag "-m" (format "Release %s" ver)]})
    (b/git-process {:git-args "push"})
    (update-readme ver)
    (update-pubspec ver)
    (update-changelog ver notes)
    (b/git-process {:git-args ["commit" "-a" "-m" (format "update doc refs to ver %s" ver)]})
    (b/git-process {:git-args ["push" "--follow-tags"]})))
