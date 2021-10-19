(ns dev-tutorial)

(comment

  '(syntax
     "Text explaining something in the Sandbox."
     {:interactive? true})

  '(syntax
     [:text "Text explaining something in the Sandbox."]
     {:interactive? true})

  '(syntax
     [:caption "Tiny text for caption."]
     {:interactive? true})

  '(syntax
     [:button
      {:action :edit}
      "(inc 1)"]
     {:interactive? true})

  '(syntax
     [:button
      {:text "Edit action"
       :action :edit}
      "(inc 1)"]
     {:interactive? true})

  '(syntax
     [:button
      "(inc 1)"]
     {:interactive? true})

  '(syntax
     [:button
      {:action :query}
      "(inc 1)"]
     {:interactive? true})


  '(syntax
     [:button
      {:text "(inc 1)"
       :action :query}
      (str '(let [x (inc 1)]
              (syntax
                [:v-box
                 [:p "Result is" [:code x]]

                 [:button
                  {:text "Finish"}
                  ":Finish"]]

                {:interactive? true
                 :clear-screen? true})))]

     {:interactive? true
      :clear-screen? true})


  '(syntax
     [:button
      {:text "Increment (Query)"
       :action :query}
      "(inc 1)"]
     {:interactive? true})


  '(syntax
     [:markdown "# Title\n\n## Subtitle"]
     {:interactive? true})


  ;; Tutorial example

  '(syntax
     [:v-box
      [:markdown
       "### Convex Lisp Tutorial\n\nLet's define a function `f`:"]

      [:h-box

       [:code
        "(defn f [x] x)"]

       [:button
        {:action :edit
         :text "Define a function"}
        (str '(defn f [x] x))]]

      [:text
       "Click on 'Continue' once you have executed the step above."]

      [:button
       {:action :query
        :text "Continue"}
       (str '(syntax
               [:v-box
                [:text "Now let's call `f`, and pass `1` as argument:"]

                [:button
                 {:text "(f 1)"
                  :action :query}

                 (str '(let [x (f 1)]
                         (syntax
                           [:v-box
                            [:code x]

                            [:button
                             {:text "Finish"}
                             ":Finish"]]

                           {:interactive? true
                            :clear-screen? true})))]]

               {:interactive? true
                :clear-screen? true}))]]

     {:interactive? true
      :clear-screen? true})

  )