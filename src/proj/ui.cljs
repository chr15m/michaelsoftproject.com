(ns proj.ui
  (:require
    [proj.data :as data]
    [reagent.core :as r]
    [reagent.dom :as rdom]))

(defonce state (r/atom {}))

(defn create-project [_ev]
  (swap! state assoc :project (data/make-project)))

(defn create-task [tasks]
  (let [task (data/make-task)]
    (swap! tasks assoc (:id task) task)))

(defn component-tasks-table [tasks]
  [:div
   [:table
    [:thead
     [:tr
      [:td "Task"]
      [:td "Who"]
      [:td "progress"]
      [:td "Duration"]
      [:td "Start"]
      [:td "End"]]]
    [:tbody
     (for [{:keys [task who progress start duration]} @tasks]
       [:tr
        [:td task]
        [:td who]
        [:td progress]
        [:td duration]
        [:td start]
        [:td "end"]])]]
   [:button {:on-click #(create-task tasks)} "Add task"]])

(defn component-home [state]
  [:div
   [:h1 "Michaelsoft Project"]
   [:p "A simple Gantt chart planner. No sign up required."]
   [:button {:on-click create-project} "new project"]
   [:pre (pr-str @state)]  
   ;[:p [:a {:href "/mypage"} "Static server rendered page."]]
   ;[:p [:a {:href "/api/example.json"} "JSON API example."]]
   ])

(defn component-project [state]
  (let [{:keys [title company lead]} @state
        tasks (r/cursor state [:tasks])]
    [:div
     [:h1 (or title "Untitled project")]
     [:h2 (or company "Company name")]
     [:h3 (or lead "Project lead")]
     [component-tasks-table tasks]]))

(defn component-main [state]
  (if (@state :project)
    [component-project state]
    [component-home state]))

(defn start {:dev/after-load true} []
  (rdom/render [component-main state]
               (js/document.getElementById "app")))

(defn main! []
  (start))
