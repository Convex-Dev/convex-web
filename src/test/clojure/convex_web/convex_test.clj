(ns convex-web.convex-test
  (:require 
   [clojure.test :refer [deftest is testing use-fixtures]]
   
   [convex-web.convex :as convex]
   [convex-web.system :as sys]
   [convex-web.test :refer [catch-throwable make-system-fixture]])
  
  (:import 
   (convex.core.data Address Blob Syntax Maps Symbol Keyword Vectors AString Strings AccountKey)
   (convex.core Result)
   (convex.core.init Init)
   (convex.core.lang Core)
   (convex.core.data.prim CVMLong CVMBool)
   (convex.core.crypto AKeyPair)
   (clojure.lang ExceptionInfo)))

(def system nil)

(use-fixtures :once (make-system-fixture #'system))

(deftest key-pair-data-test
  (testing "Roundtrip"
    (let [^AKeyPair generated (convex/generate-key-pair)]
      (is (= (.getAccountKey generated)
            (.getAccountKey (-> generated
                              convex/key-pair-data
                              convex/create-key-pair)))))))

(deftest read-source-test
  (is (= [] (convex/read-source "()")))
  (is (= [] (convex/read-source "[]")))

  (testing
    "Blob literal"
    (is
      (= (Blob/fromHex
           "Fd0cfE9EDf767823b927a25D6840ae2367455c29A7B77CBBF146Be3EeF270356")
         (convex/read-source
           "0xFd0cfE9EDf767823b927a25D6840ae2367455c29A7B77CBBF146Be3EeF270356")))

    (let [e (try (convex/read-source "0xABC")
                 (catch ExceptionInfo ex ex))]
      (is
        (= "Reader error: Invalid Blob syntax: 0xABC"
          (ex-message e)))
      (is (= {:cognitect.anomalies/category :cognitect.anomalies/incorrect,
              :convex-web.result/error-code :READER}
             (ex-data e)))))

  (testing "Address literal"
           (is (= (Address/create 8) (convex/read-source "#8"))))

  (testing "Read all with do"
           (is (= [(Symbol/create "do") (CVMLong/create 1) (CVMLong/create 2)]
                  (convex/read-source "1 2")))))

(deftest lookup-metadata-test
  (let [context (sys/convex-world-context system)]
    (is
      (= '{:doc
           {:description "Applies a Function to each element of a data structure in sequence, and returns a vector of results. Additional collections may be provided to call a function with higher arity.",
            :examples [{:code "(map inc [1 2 3])"}],
            :signature [{:params [f coll]}
                        {:params [f coll1 coll2 & more-colls]}]}
           :static true}
        (convex/datafy (convex/lookup-metadata context (Symbol/create "map")))))))

(deftest datafy-test
  (let [context (sys/convex-world-context system)]
    (testing "nil" (is (= nil (convex/datafy nil))))
    
    (testing "Byte"
      (is (= convex.core.data.prim.CVMByte (type (convex/execute-string context "(nth 0xFF 0)"))))
      (is (= 255 (convex/datafy (convex/execute-string context "(nth 0xFF 0)")))))
    
    (testing "Char" 
      (is (= \n (convex/datafy (convex/execute context \n)))))
    
    (testing "Long" 
      (is (= 1 (convex/datafy (convex/execute context 1)))))
    
    (testing "Double" 
      (is (= 1.0 (convex/datafy (convex/execute context 1.0)))))
    
    (testing "Keyword" 
      (is (= :a (convex/datafy (convex/execute context :a)))))
    
    (testing "Symbol"
      (is (= 's (convex/datafy (convex/execute-string context "'s"))))
      (is (= '(lookup a b) (convex/datafy (convex/execute-string context "'a/b"))))
      (is (= '(lookup 8 inc) (convex/datafy (convex/execute-string context "'#8/inc")))))
    
    (testing "List" 
      (is (= '() (convex/datafy (convex/execute context '())))))
    
    (testing "Vector" 
      (is (= [] (convex/datafy (convex/execute context [])))))
    
    (testing "Map"
      (is (= {} (convex/datafy (convex/execute context {}))))
      (is (= {'(1) '(2)} (convex/datafy (convex/execute context {(list 1) (list 2)})))))
    
    (testing "Set" 
      (is (= #{} (convex/datafy (convex/execute context #{}))))
      (is (= #{1} (convex/datafy (convex/execute-string context "#{1 1}"))))
      (is (= #{[:a 1]} (convex/datafy (convex/execute-string context "#{[:a 1]}")))))
    
    #_(testing "AccountKey"
        (is (= (-> Init/HERO_KP
                 .getAccountKey
                 .toChecksumHex)
              (convex/datafy (.getAccountKey Init/HERO_KP)))))
    
    (testing "Address"
      (is (= (.longValue Init/RESERVED_ADDRESS) 
            (convex/datafy Init/RESERVED_ADDRESS))))
    
    (testing "Blob"
      (is (= "0x1234" (convex/datafy (Blob/fromHex "1234"))))

      (is (= (str "0x" (.toHexString (Blob/create (.getBytes "Text"))))
            (convex/datafy (Blob/create (.getBytes "Text"))))))
    
    (testing "Syntax"
      (is (= 1 (convex/datafy (Syntax/create (CVMLong/create 1)))))
      (is (= (Maps/empty) (convex/datafy (Syntax/create (Maps/empty))))))
    
    (testing "Non-CVM types will throw"
      (is (= "Can't datafy :x clojure.lang.Keyword."
            (ex-message (catch-throwable (convex/datafy :x)))))
      (is (= "Can't datafy () clojure.lang.PersistentList$EmptyList."
            (ex-message (catch-throwable (convex/datafy '())))))
      (is (= "Can't datafy 1 java.lang.Long."
            (ex-message (catch-throwable (convex/datafy 1)))))
      (is (= "Can't datafy 1.0 java.lang.Double."
            (ex-message (catch-throwable (convex/datafy 1.0)))))
      (is (= "Can't datafy \"ABC\" java.lang.String."
            (ex-message (catch-throwable (convex/datafy "ABC"))))))))

(deftest address-test
  (testing "Coerce string to Address"
           (is (= (convex/address "1") (Address/create 1)))
           (is (= (convex/address "#1") (Address/create 1))))

  (testing "No coercion"
           (is (= (convex/address (Address/create 1)) (Address/create 1))))

  (testing "Can't coerce nil"
           (is (= "Can't coerce nil to convex.core.data.Address."
                  (ex-message (catch-throwable (convex/address nil))))))

  (testing "Can't coerce empty string"
           (is (= "Can't coerce empty string to convex.core.data.Address."
                  (ex-message (catch-throwable (convex/address ""))))))

  (testing "Invalid Address string"
           (is (= "Can't coerce \"foo\" to convex.core.data.Address."
                  (ex-message (catch-throwable (convex/address "foo")))))))


(deftest rollback-test
  (testing
    "Rollback exceptional value"
    (let [context1 (sys/convex-world-context system)
          
          context2 (convex/execute-string* context1 "(def x 1)")
          
          context3 (convex/execute-string* context2
                     "(do (def x 2) (rollback :abort))")]
      
      (is (= (Keyword/create "abort")
            (.getValue (.getExceptional context3)))))))

(deftest coerce-element-test
  (testing "Text syntax sugar"
    (is (= [:text "Hello"] (convex/coerce-element "Hello"))))

  (testing "Horizontal layout & text syntax sugar"
    (is (= [:p
            [:text "Hello"]
            [:text "World"]]
          (convex/coerce-element ["Hello" "World"]))))

  (testing "Mix"
    (is (= [:p
            [:text "Hello"]
            [:text "World"]
            [:text "Foo"]
            [:p
             [:text "Bar"]
             [:text "Baz"]]]
          (convex/coerce-element ["Hello" "World" [:text "Foo"] ["Bar" [:text "Baz"]]])))))

(deftest result-data-test
  (testing "Bool"
    (is (= {:convex-web.result/id 1,
            :convex-web.result/type "Boolean",
            :convex-web.result/value "true"}
          (convex/result-data (Result/create (CVMLong/create 1)
                                (CVMBool/create true))))))

  (testing "String"
    (is (= {:convex-web.result/id 1,
            :convex-web.result/type "String",
            :convex-web.result/value "\"1\""}
          (convex/result-data (Result/create (CVMLong/create 1)
                                (Strings/create "1"))))))
  
  (testing "Long"
    (is (= {:convex-web.result/id 1,
            :convex-web.result/type "Long",
            :convex-web.result/value "1"}
          (convex/result-data (Result/create (CVMLong/create 1)
                                (CVMLong/create 1))))))
  
  (testing "Keyword"
    (is (= {:convex-web.result/id 1,
            :convex-web.result/type "Keyword",
            :convex-web.result/value ":a"}
          (convex/result-data (Result/create (CVMLong/create 1)
                                (Keyword/create "a"))))))
  
  (testing "Address"
    (is (= {:convex-web.result/id 1,
            :convex-web.result/type "Address",
            :convex-web.result/value "#1"}
          (convex/result-data (Result/create (CVMLong/create 1)
                                (Address/create 1))))))
  
  (testing "Vector"
    (is (= {:convex-web.result/id 1,
            :convex-web.result/type "Vector",
            :convex-web.result/value "[]"}
          (convex/result-data (Result/create (CVMLong/create 1)
                                (Vectors/empty))))))
  
  (testing "Map"
    (is (= {:convex-web.result/id 1,
            :convex-web.result/type "Map",
            :convex-web.result/value "{}"}
          (convex/result-data (Result/create (CVMLong/create 1)
                                (Maps/empty))))))
  
  (testing "Syntax"
    (is (= {:convex-web.result/id 1,
            :convex-web.result/type "Syntax",
            :convex-web.result/value "^{} {}"
            :convex-web.result/metadata {}}
          (convex/result-data (Result/create (CVMLong/create 1)
                                (Syntax/create (Maps/empty))))))

    (testing "Interactive"
      (is (= {:convex-web.result/id 1,
              :convex-web.result/type "Syntax",
              :convex-web.result/value "^{:interact? true} {}"
              :convex-web.result/metadata {:interact? true}}
            (select-keys
              (convex/result-data (Result/create (CVMLong/create 1)
                                    (Syntax/create (Maps/empty)
                                      ;; Metadata to mark this Syntax as interactive.
                                      (.assoc (Maps/empty)
                                        (Keyword/create "interact?") (CVMBool/create true)))))
              [:convex-web.result/id
               :convex-web.result/type
               :convex-web.result/value
               :convex-web.result/metadata])))))

  (testing
    "Core function"
    (is
      (=
        '{:convex-web.result/id 1,
          :convex-web.result/metadata
          {:static true
           :doc
           {:description
            "Applies a Function to each element of a data structure in sequence, and returns a vector of results. Additional collections may be provided to call a function with higher arity.",
            :examples [{:code "(map inc [1 2 3])"}],
            :signature [{:params [f coll]}
                        {:params [f coll1 coll2 & more-colls]}]}},
          :convex-web.result/type "Function",
          :convex-web.result/value "map"}
        
        (convex/result-data (Result/create (CVMLong/create 1) Core/MAP))))))

(deftest account-key-from-hex-test
  (testing "Invalid Address"
    (is (thrown? NullPointerException (convex/account-key-from-hex nil)))
    
    (is (= "Invalid Address hex String []" 
          (try 
            (convex/account-key-from-hex "")
            (catch Throwable ex
              (ex-message ex)))))
    
    (is (= "Invalid Address hex String [123]" 
          (try 
            (convex/account-key-from-hex "123")
            (catch Throwable ex
              (ex-message ex))))))
  
  (testing "Roundtrip"
    (let [^String account-key-hex "85cd134e1500ecf935677908cbd2b45c32622455fe55e8038ad45917250f72ea"
          ^AccountKey account-key (convex/account-key-from-hex account-key-hex)]
      (is (= account-key-hex (.toHexString account-key))))))
