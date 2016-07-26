(defproject wxservice "0.1.0-SNAPSHOT"
  :description "Weixin Service"
  :source-paths ["src"]
  :java-source-paths ["wxsdk"]
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
                 [compojure "1.5.1"]
                 ;; used by wxsdk
                 [commons-codec/commons-codec "1.10"]
                 [junit/junit "4.12"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler liu.wx.core/handler}
  :main ^:skip-aot liu.wx.core
  :profiles {:uberjar {:aot :all}}
  :javac-options ["-encoding" "utf-8"])
