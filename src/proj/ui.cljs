(ns proj.ui
  (:require
    [shadow.resource :as rc]
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

(defn edit-task [tasks idx]
  (swap! tasks update-in [idx :editing] not))

(defn component-icon [svg]
  [:span {:class "icon"
          :ref (fn [el] (when el (aset el "innerHTML" svg)))}])

(defn component-task-header [days]
  [:tr
   [:td ""]
   [:td "Task"]
   [:td "Resource"]
   [:td "Start"]
   [:td "End"]
   [:td]
   #_ [:td "Progress"]
   #_ [:td "Parent"]
   #_ [:td "Days"]
   (doall
     (for [d (range (max 31 days))]
       [:td {:key d} d]))])

(defn component-task-edit [_project-start [start end] tasks idx task _days]
  [:tr.edit
   [:td (inc idx)]
   [:td [:input (data/editable tasks [idx :task] {:placeholder "Task name"})]]
   [:td [:input (data/editable tasks [idx :who] {:placeholder "Resource"})]]
   [:td [:span (data/format-date start)]]
   [:td [:span (data/format-date end)]]
   [:td [:button {:on-click #(edit-task tasks idx)}
         [component-icon (rc/inline "check.svg")]]]
   [:td {:colspan 200}
    [:span
     [:label "Duration"]
     [:input (data/editable tasks [idx :duration]
                            {:type "number"
                             :placeholder "Duration"
                             :min 0
                             :max 1000})]]
    [:span
     [:label "Predecessor"]
     [:input (data/editable tasks [idx :parent]
                            {:class (when (= start :error) :error)
                             :type "number"
                             :placeholder "Predecessor"
                             :min 0
                             :max 1000})]]
    #_ [:span
        [:label "Complete"]
        [:input (data/editable tasks [idx :progress]
                               {:type "number"
                                :placeholder "Percent done"
                                :min 0
                                :max 100})]]
    [:button.warning {:on-click #(remove-task tasks (:id task))}
     [component-icon (rc/inline "trash.svg")]]]])

(defn component-task-show [project-start [start end] tasks idx _task days]
  [:tr
   [:td (inc idx)]
   [:td [:input (data/editable tasks [idx :task] {:placeholder "Task name"})]]
   [:td [:input (data/editable tasks [idx :who] {:placeholder "Resource"})]]
   [:td [:span (data/format-date start)]]
   [:td [:span (data/format-date end)]]
   [:td [:button {:on-click #(edit-task tasks idx)}
         [component-icon (rc/inline "pencil.svg")]]]
   (doall
     (for [d (range (max 31 days))]
       (let [date (data/end-date project-start d)
             filled (and
                      start end date
                      (not= start :error) (not= end :error)
                      (<= (.getTime start) (.getTime date))
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
     (when (seq @tasks)
       [:table
        [:thead
         [component-task-header days]]
        [:tbody
         (doall
           (for [idx (range (count @tasks))]
             (let [task (nth @tasks idx)
                   id (:id task)]
               (if (:editing task)
                 (with-meta [component-task-edit start (get start-end-map id) tasks idx task days] {:key id})
                 (with-meta [component-task-show start (get start-end-map id) tasks idx task days] {:key id})))))]])
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
   [:> Twemoji "????"]
   ;[:pre (pr-str @state)]
   ;[:p [:a {:href "/mypage"} "Static server rendered page."]]
   ;[:p [:a {:href "/api/example.json"} "JSON API example."]]
   ])

(defn component-project [state]
  (let [project (r/cursor state [:project])
        tasks (r/cursor state [:project :tasks])
        start (get-in @state [:project :start])]
    [:div#planner
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
