class MapCategory {
    static void add(Map map, key, value) {
        if(map.containsKey(key)) {
        	map.get(key).add(value)
        } else {
        	def set = new LinkedHashSet()
        	set.add(value)
        	map.put(key, set)
        }
    }
}

