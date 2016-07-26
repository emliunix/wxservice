(ns liu.wx.util)

(defn hexstr [data-bytes]
  (apply str 
         (map 
          #(.substring 
            (Integer/toString 
             (+ (bit-and % 0xff) 0x100) 16) 1) 
          data-bytes)))

(defn cat-bytes [& bytes-list]
  (let [result (byte-array (->> bytes-list
                                (map alength)
                                (reduce +)))]
    (loop [idx 0
           [curr & more] bytes-list]
      (let [len-curr (count curr)]
        (when (not (nil? curr))
          (System/arraycopy curr 0 result idx len-curr)
          (recur (+ idx len-curr) more))))
    result))
