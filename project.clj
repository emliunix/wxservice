(defproject wxservice "0.1.0-SNAPSHOT"
  :description "Weixin Service"
  :source-paths ["src"]
  :test-paths ["test"]
  :resource-paths ["resources"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.xml "0.0.8"]
                 ;; utils
                 [org.clojure/tools.logging "0.3.1"]
                 ;; database
                 [korma "0.4.2"]
                 [org.postgresql/postgresql "9.4.1209"]
                 ;; web framework
                 [ring/ring-core "1.5.0"]
                 [ring/ring-devel "1.5.0"]
                 [ring/ring-servlet "1.5.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 ;; used for encryption, decryption, hash, etc
                 [commons-codec/commons-codec "1.10"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler liu.wx.core/app}
  :main ^:skip-aot liu.wx.core
  :profiles {:uberjar {:aot :all}}
  :javac-options ["-encoding" "utf-8"])
