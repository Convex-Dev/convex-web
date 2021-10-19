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
      {:text "Increment (Query)"
       :action :query}
      "(inc 1)"]
     {:interactive? true})


  '(syntax
     [:markdown "# Title\n\n## Subtitle"]
     {:interactive? true})

  '(syntax
     [:v-box
      [:markdown
       "### Convex Lisp Tutorial\n\nLet's define a function `f`:"]

      [:h-box

       [:code
        "(defn f [x] x)"]

       [:button
        {:action :edit
         :source '(defn f [x] x)}
        "Define a function"]]

      [:caption
       "Click on 'Continue' once you have executed the step above."]

      [:button
       {:action :query
        :source
        '(syntax
           [:v-box
            [:text "Now let's call `f`, and pass `1` as argument:"]

            [:button
             {:action :edit
              :source '(f 1)}
             "Call a function"]]

           {:interactive? true})}
       "Continue"]]

     {:interactive? true})

  )