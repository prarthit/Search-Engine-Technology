package cecs429.indexing.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TermPositionCrud implements TermPositionDao {

    private static Connection con;
    private static PreparedStatement ps;
    public void openConnection() throws SQLException {
        con = SQLiteDatabaseConnection.getConnection();
        con.setAutoCommit(false);
    }

    private String directoryName;

    public TermPositionCrud(String directoryName) {
        this.directoryName = directoryName;
    }

    public void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS `" + directoryName + "-TermBytePosition` (\n"
                + " id integer PRIMARY KEY,\n"
                + " term text NOT NULL,\n"
                + " byte_position integer\n"
                + ");";

        try {
            Statement stmt = con.createStatement();
            stmt.execute(sql);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void add(TermPositionModel tpm) throws SQLException {
        String query = "insert into `" + directoryName + "-TermBytePosition`(term, "
                + "byte_position) VALUES (?, ?)";
        try{
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, tpm.getTerm());
            ps.setLong(2, tpm.getBytePosition());
            ps.executeUpdate();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void add(String term, long byte_position) throws SQLException {
        try{
            ps.setString(1, term);
            ps.setLong(2, byte_position);
            ps.addBatch();
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public TermPositionModel getTermPositionModel(String term) throws SQLException {
        String query = "select * from `" + directoryName + "-TermBytePosition` where term = ?";
        PreparedStatement ps = con.prepareStatement(query);

        ps.setString(1, term);
        TermPositionModel tpm = new TermPositionModel();
        ResultSet rs = ps.executeQuery();
        boolean check = false;

        while (rs.next()) {
            check = true;
            tpm.setId(rs.getInt("id"));
            tpm.setTerm(rs.getString("term"));
            tpm.setBytePosition(rs.getLong("byte_position"));
        }

        if (check == true) {
            return tpm;
        } else
            return null;
    }

    public void getAllTermPositionData(HashMap<String, Long> hm) throws SQLException {
        String query = "select * from `" + directoryName + "-TermBytePosition`";
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            String t = rs.getString("term");
            Long b = rs.getLong("byte_position");
            hm.put(t, hm.getOrDefault(t,b));
        }
    }

    public long getBytePositionFromModel(String term) throws SQLException {
        String query = "select byte_position from `" + directoryName + "-TermBytePosition` where term = ?";
        PreparedStatement ps = con.prepareStatement(query);

        ps.setString(1, term);
        ResultSet rs = ps.executeQuery();

        if(!rs.next()){
            return -1;
        }
        return rs.getLong("byte_position");
    }

    @Override
    public List<String> getVocabularyTerm() throws SQLException {
        String query = "select term from `" + directoryName + "-TermBytePosition` order by term";
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        List<String> ls = new ArrayList<>();

        while (rs.next()) {
            ls.add(rs.getString("term"));
        }
        return ls;
    }
    @Override
    public void update(TermPositionModel tpm) throws SQLException {
        String query = "update `" + directoryName + "-TermBytePosition` set term=?, "
                + " byte_position= ? where id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, tpm.getTerm());
        ps.setLong(2, tpm.getBytePosition());
        ps.setInt(3, tpm.getId());
        ps.executeUpdate();
    }

    public void initializePreparestatement() {
        String query = "insert into `" + directoryName + "-TermBytePosition`(term, "
                + "byte_position) VALUES (?, ?)";
        try{
            ps = null;
            ps = con.prepareStatement(query);
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void executeInsertBatch(){
        try {
            ps.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sqlCommit() throws SQLException{
        con.commit();
        con.setAutoCommit(true);
    }
}
