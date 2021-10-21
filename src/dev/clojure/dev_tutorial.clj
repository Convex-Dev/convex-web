(ns dev-tutorial)

(comment

  '(syntax
     "Text explaining something in the Sandbox."
     {:interact? true
      :cls? true})

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
     {:interact? true
      :mode :query
      :input "(inc 1)"})

  '(syntax
     [:cmd
      {:name "Execute"
       :mode :query
       :show-source? true}
      "(inc 1)"]
     {:interact? true})

  ;; -- Frame

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

  ;; -- Lang

  '(syntax
     [:cmd
      {:name "Execute"
       :mode :query
       :show-source? true
       :lang (str '(fn [x] x))}
      (str '(inc 1))]
     {:interact? true
      :cls? true})

  '(syntax
     [:cmd
      {:name "Execute"
       :mode :query
       :show-source? true
       :lang (str '(fn [x]
                     (syntax
                       [:v-box
                        [:code x]
                        [:cmd
                         {:mode :query
                          :name "Done"}
                         "nil"]]
                       {:interact? true
                        :cls? true})))}
      (str '(inc 1))]
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
       "(defn f [x] x)"]

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
                            [:text "Result:"]
                            [:code x]

                            [:cmd
                             {:name "Finish"
                              :mode :query}
                             ":Finish"]]

                           {:interact? true
                            :cls? false
                            :mode :query})))]]

               {:interact? true
                :cls? true
                :mode :query}))]]

     {:interact? true
      :cls? true
      :mode :query
      :input "(defn f [x] x)"})

  )