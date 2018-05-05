package practice.restapi.JunitWithPowerMockito;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

final public class ConnectionPool {
    
    private static ConnectionPool connectionPool = new ConnectionPool();
    private List<Connection> connList = new ArrayList<>();
    
    private ConnectionPool() {}
    
    public void addConnectionToPool(Connection con) {
        // This method is adding connection to pool.
        connList.add(con);
    }
    
    public void removeConnectionToPool(Connection con) {
        // This method is removing connection to pool.
        connList.remove(con);
    }

    public static ConnectionPool getInstance() {
        return connectionPool;
    }
    
    public int getNoOfConnectionPresent() {
        return connList.size();
    }
    
    public Connection getConnection(){
       Connection con = connectionPool.getConnection();
       removeConnectionToPool(con);
       return con;
    }
}
