import tools.util.sql.parser.*
import org.apache.commons.collections.map.*
import org.apache.commons.collections.set.*

class SqlCategory {
	
	static def getAllQuery(ASTSqlNode node) {
		node.findAllRecurse {
			it instanceof ASTQuery && it.isSimple()
		}
	}
	
	static def findAllRecurse(ASTSqlNode node, Closure cl) {
		return node.findAllRecurse(cl as ASTSqlNode.NodeFilter)
	}
	
	static def findAll(ASTSqlNode node, Closure cl) {
		return node.findAll(cl as ASTSqlNode.NodeFilter)
	}
	
	static void eachRecurse(ASTSqlNode node, Closure cl) {
		node.eachRecurse(cl as ASTSqlNode.NodeVisitor)
	}
	
	static void each(ASTSqlNode node, Closure cl) {
		node.each(cl as ASTSqlNode.NodeVisitor)
	}
	
	static def getChildRecurse(ASTSqlNode node) {
		def list = []
		node.each {
			if(!(it instanceof ASTQuery)) {
				list.addAll(it.getChildRecurse())
			}
			list.add(it)
		}
		return list
	}
	
	static def getChildList(ASTSqlNode node) {
		node.findAll {
			true
		}
	}
	
	static def getFromMap(ASTQuery query) {
		def map = new CaseInsensitiveMap()
		query.from.each {table->
			map.add("", table)
			map.add(null, table)
			def alias = table.getAlias()
			if (alias == null) {
				if (table instanceof ASTTable) {
					map.add(table.getName(), table)
				}
			} else {
				map.add(alias, table)
			}
		}
		return map
	}
	
	static def getJoins(ASTQuery query) {
		query.getTwoSide().findAll {
			it.isJoin()
		}.collect {
			it.getJoin()
		}
	}
	
	static def getTwoSide(ASTSqlNode node) {
		node.getChildRecurse().findAll {
			it instanceof ASTTwoSide
		}
	}
	
	static def isFixedRow(ASTQuery query) {
		if(query.isUnioned()) {
			return query.findAll {
				it instanceof ASTQuery
			}.every {
				it.isFixedRow()
			}
		}
		if(query.from.every {
			it instanceof ASTTable && it.name.upper() == 'DUAL'
		}) {
			return true
		}
		def aggrigate = query.select.collect {
			it.value
		}.every {
			it.isAggrigate()
		}
		if(aggrigate && query.groupBy == null) {
			return true
		}
		return false
	}
	
	static boolean isAggrigate(ASTFunction func) {
		return func.name.upper() in ["SUM", "COUNT", "MAX", "MIN"] || func.findAll {
			it instanceof ASTFunction
		}.every {
			it.isAggrigate()
		}
	}
	
	static boolean isAggrigate(ASTExp exp) {
		if(exp instanceof ASTFunction) {
			return exp.isAggrigate()
		}
		if(exp instanceof ASTColumn) {
			return false
		}
		def cols = exp.findAllRecurse(ASTColumn)
		if(cols) {
			return cols.every {
				it.ancestor(ASTFunction)?.isAggrigate()
			}
		}
		return false
	}
	
	static def parseSql(File sqlFile) {
		def node
		sqlFile.withInputStream {
			SqlParser parser = new SqlParser(it, "UTF-8")
			node = parser.sql()
		}
		return node
	}
	
	static def parseSql(String sql) {
		def sr = new StringReader(sql)
		SqlParser parser = new SqlParser(sr)
		return parser.sql()
	}
}

