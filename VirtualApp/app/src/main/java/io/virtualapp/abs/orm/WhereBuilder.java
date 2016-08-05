package io.virtualapp.abs.orm;

import java.util.List;

/**
 * @author Lody
 * @version 1.0
 */
public class WhereBuilder {

	private final Orm dao;
	private StringBuilder statement;
	private Class<?> beanClass;

	public WhereBuilder(Orm welikeDao, Class<?> beanClass) {
		this.dao = welikeDao;
		statement = new StringBuilder();
		this.beanClass = beanClass;
	}

	public WhereBuilder where(String where) {
		statement.append(where);
		return this;
	}

	public WhereBuilder and(String appendStatement) {
		statement.append(" and ").append(appendStatement);
		return this;
	}

	public WhereBuilder or(String appendStatement) {
		statement.append(" or ").append(appendStatement);
		return this;
	}

	public WhereBuilder orderBy(String appendStatement) {
		statement.append(" order by ").append(appendStatement);
		return this;
	}

	public <T> List<T> find() {
		return dao.many(beanClass, statement.toString());
	}

}
