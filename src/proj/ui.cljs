(ns proj.ui
  (:require
    [proj.data :as data]
    ["react-twemoji$default" :as Twemoji]
    [sitefox.ui :refer [log]]
    [reagent.core :as r]
    [reagent.dom :as rdom]))

(defonce state (r/atom {}))

(defn create-project [_ev]
  (swap! state assoc :project (data/make-project)))

(defn create-task [tasks]
  (swap! tasks conj (data/make-task)))

(defn remove-task [tasks id]
  (swap! tasks (fn [old-tasks]
                 (vec
                   (remove #(= id (:id %)) old-tasks)))))

(defn component-task-header [days]
  [:tr
   [:td ""]
   [:td "Task"]
   [:td "Who"]
   [:td "Progress"]
   [:td "Parent"]
   [:td "Days"]
   [:td "Start"]
   [:td "End"]
   [:td]
   (doall
     (for [d (range (max 14 days))]
       [:td {:key d} d]))])

(defn component-task-edit [project-start [start end] tasks idx task days]
  [:tr
   [:td (inc idx)]
   [:td [:input (data/editable tasks [idx :task])]]
   [:td [:input (data/editable tasks [idx :who])]]
   [:td [:input (data/editable tasks [idx :progress]
                               {:type "number"
                                :min 0
                                :max 100})]]
   [:td [:input (data/editable tasks [idx :parent]
                               {:class (when (= start :error) :error)
                                :type "number"
                                :min 0
                                :max 1000})]]
   [:td [:input (data/editable tasks [idx :duration]
                               {:type "number"
                                :min 0
                                :max 1000})]]
   [:td [:span (data/format-date start)]]
   [:td [:span (data/format-date end)]]
   [:td [:button {:on-click #(remove-task tasks (:id task))} "X"]]
   (doall
     (for [d (range (max 14 days))]
       (let [date (data/end-date project-start d)
             filled (and (<= (.getTime start) (.getTime date))
                         (> (.getTime end) (.getTime date)))]
         [:td.map {:key d
                   :class (when filled :fill)}])))])

(defn component-tasks-table [start tasks]
  (let [start-end-map (into {} (map (fn [task] {(:id task) (data/compute-date-range start @tasks task)}) @tasks))
        last-date (->> start-end-map
                       vals
                       (map second)
                       (map #(js/Date. %))
                       (apply max))
        seconds (- last-date (js/Date. start))
        days (/ seconds (* 1000 60 60 24))]
    (log days)
    [:div
     [:table
      [:thead
       [component-task-header days]]
      [:tbody
       (doall
         (for [idx (range (count @tasks))]
           (let [task (nth @tasks idx)
                 id (:id task)]
             (with-meta [component-task-edit start (get start-end-map id) tasks idx task days] {:key id}))))]]
     [:button {:on-click #(create-task tasks)} "Add task"]]))

(defn component-footer []
  [:footer
   [:p "This is a prototype. Tell me what to fix "
    [:a {:href "mailto:chris@mccormick.cx"} "by email"]
    " or "
    [:a {:href "https://twitter.com/mccrmx/status/1495756501947977729"
         :target "_BLANK"}
     "on Twitter"]
    "."]])

(defn component-home [_state]
  [:div#landing
   [:h1 "Michaelsoft Project"]
   [:p "A simple online Gantt chart planner. No sign up required. For web not Binbows."]
   [:p
    [:button {:on-click create-project} "new project"]]
   [:p]
   [:> Twemoji "🤠"]
   ;[:pre (pr-str @state)]
   ;[:p [:a {:href "/mypage"} "Static server rendered page."]]
   ;[:p [:a {:href "/api/example.json"} "JSON API example."]]
   ])

(defn component-project [state]
  (let [project (r/cursor state [:project])
        tasks (r/cursor state [:project :tasks])
        start (get-in @state [:project :start])]
    [:div
     [:h1 [:input (data/editable project [:title] {:placeholder "Untitled project"})]]
     [:h2 [:input (data/editable project [:company] {:placeholder "Company name"})]]
     [:h3 [:input (data/editable project [:lead] {:placeholder "Project lead"})]]
     [:p [:label "Start: " [:input (data/editable project [:start]
                                                  {:type "date"
                                                   :defaultValue (data/today)})]]]
     [component-tasks-table start tasks]
     [component-footer]
     ;[:pre (pr-str @tasks)]
     ]))

(defn component-main [state]
  (if (@state :project)
    [component-project state]
    [component-home state]))

(defn start {:dev/after-load true} []
  (rdom/render [component-main state]
               (js/document.getElementById "app")))

(defn main! []
  (start))
