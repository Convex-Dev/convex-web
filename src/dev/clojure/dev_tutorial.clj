(ns dev-tutorial)

(comment

  '(syntax
     "Text explaining something in the Sandbox."
     {:interact? true})

  '(syntax
     [:text "Text explaining something in the Sandbox."]
     {:interact? true})

  '(syntax
     ["Text explaining something in the Sandbox.       "
      [:cmd
       {:show-source? true}
       "(inc 1)"]
      " Text explaining something in the Sandbox. "
      " Text explaining something in the Sandbox. "
      " Text explaining something in the Sandbox. "
      [:cmd
       {:show-source? true}
       "(inc 1)"]]
     {:interact? true})

  '(syntax
     [:h-box
      [:text "Text explaining something in the Sandbox."]
      [:text "Text explaining something in the Sandbox."]
      [:text "Text explaining something in the Sandbox."]
      [:text "Text explaining something in the Sandbox."]
      [:cmd
       "(inc 1)"]]
     {:interact? true})

  '(syntax
     [:cmd
      {:name "Execute"
       :mode :query
       :show-source? false}
      "(inc 1)"]
     {:interact? true})

  '(syntax
     [:cmd
      {:name "Execute"
       :mode :query
       :show-source? true}
      "(inc 1)"]
     {:interact? true})

  '(syntax
     [:cmd
      {:name "Execute"
       :mode :query
       :show-source? true
       :frame (str '(syntax [:v-box

                             [:code :%]

                             [:cmd
                              {:mode :query :name "Done"}
                              ":done"]]
                      {:interact? true
                       :cls? true}))}
      "(inc 1)"]
     {:interact? true
      :cls? true})


  '(syntax
     [:md "# Title\n\n## Subtitle"]
     {:interact? true})


  ;; Tutorial example

  '(syntax
     [:v-box
      [:md
       "### Convex Lisp Tutorial\n\nLet's define a function `f`:"]

      [:cmd
        {:name "Execute"
         :show-source? true}
        (str '(defn f [x] x))]

      [:text
       "Click on 'Continue' once you have executed the step above."]

      [:cmd
       {:mode :query
        :name "Continue"}
       (str '(syntax
               [:v-box
                [:text "Now let's call `f`, and pass `1` as argument:"]

                [:cmd
                 {:name "(f 1)"
                  :mode :query}

                 (str '(let [x (f 1)]
                         (syntax
                           [:v-box
                            [:code x]

                            [:cmd
                             {:name "Finish"
                              :mode :query}
                             ":Finish"]]

                           {:interact? true
                            :cls? true})))]]

               {:interact? true
                :cls? true}))]]

     {:interact? true
      :cls? true
      :mode :query
      :input "(inc 1)"})

  )