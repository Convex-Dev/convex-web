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


  ;; -- Voting System
  ;; https://convex.world/examples/voting-system

  '(defn voting-gui []
     (syntax
       [:v-box

        [:md "### What if instead of writing Convex Lisp to interface with a Smart Contract, you could, let's say, press buttons?"]

        [:cmd
         {:name "Let's see if that's possible!"}
         (str '(syntax
                 [:v-box
                  [:text "Do you like Belgian waffles?"]

                  [:cmd
                   {:name "Yes"}
                   (str '(call my-proposal (vote :yes)))]

                  [:cmd
                   {:name "Meh"}
                   (str '(call my-proposal (vote :meh)))]

                  [:cmd
                   {:name "No"}
                   (str '(call my-proposal (vote :no)))]]

                 {:interact? true
                  :cls? true}))]]

       {:interact? true
        :cls? true}))


  '(defn query-votes-gui []
     (syntax
       [:cmd
        {:name "Check votes"
         :lang (str '(fn [m]
                       (syntax [:v-box
                                [:md "### Votes"]

                                [:p [:text "Yes: "] [:text (get m :yes)]]
                                [:p [:text "Meh: "] [:text (get m :meh)]]
                                [:p [:text "No: "] [:text (get m :no)]]]
                         {:interact? true
                          :cls? false})))}
        (str 'my-proposal/votes)]
       {:interact? true
        :cls? false}))


  ;; 4Clojure
  ;; -- https://4clojure.oxal.org/#/problem/1

  '(defn problem1 []
     (syntax
       [:v-box
        [:md "### Problem 1"]

        [:md "Complete the expression so it will evaluate to true."]

        [:code "(= _ true)"]

        [:cmd
         {:name "Check"
          :mode :query
          :show-source? true
          :lang
          (str
            '(fn [x]
               (syntax
                 (if (= true x)
                   [:v-box
                    [:text "Correct! 🎉"]
                    [:cmd
                     {:name "Next"}
                     (str '(problem2))]]
                   [:text
                    "Oops.. try again."])
                 {:interact? true})))}

         ";; Type your solution here ✍️"]]

       {:interact? true
        :cls? true}))

  '(defn problem2 []
     (syntax
       [:v-box
        [:md "### Problem 2"]

        [:md "TODO"]]

       {:interact? true
        :cls? true}))



  )