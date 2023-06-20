package de.quantumrange.verbo.model.generator;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Random;

public class IdGenerator implements IdentifierGenerator {
	
	private String tableName;
	
	@Override
	public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
		IdentifierGenerator.super.configure(type, params, serviceRegistry);
		
		tableName = params.getProperty("table");
	}
	
	@Override
	public @Nullable Serializable generate(@NotNull SharedSessionContractImplementor session,
	                                       Object object) throws HibernateException {
		try {
			Random rnd = new Random();
			Connection connection = session.connection();
			
			long id;
			
			do {
				id = rnd.nextLong();
			} while (exists(id, tableName, connection) || id <= 0);
			
			return id;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static boolean exists(long id, String table, @NotNull Connection con) throws SQLException {
		Statement statement = con.createStatement();
		ResultSet resultSet = statement.executeQuery("select count(id) from " + table + " where id = " + id);
		
		return resultSet.next() && resultSet.getInt(1) != 0;
	}
	
}
