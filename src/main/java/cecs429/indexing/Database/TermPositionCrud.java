package cecs429.indexing.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TermPositionCrud implements TermPositionDao {

    private static Connection con = SQLiteDatabaseConnection.getConnection();

    public TermPositionCrud() throws SQLException{
        createTable();
    }
    
    public void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS TermBytePosition (\n"
                + " id integer PRIMARY KEY,\n"
                + " term text NOT NULL,\n"
                + " byte_position integer\n"
                + ");";

        try {
            Statement stmt = con.createStatement();
            stmt.execute(sql);
        } catch (SQLException ex) {
        }
    }
    
    @Override
    public int add(TermPositionModel tpm) throws SQLException {
        String query = "insert into TermBytePosition(term, "
                + "byte_position) VALUES (?, ?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, tpm.getTerm());
        ps.setInt(2, tpm.getBytePosition());
        int n = ps.executeUpdate();
        return n;
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "delete from TermBytePosition where id =?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public TermPositionModel getTermPositionModel(String term) throws SQLException {
        String query = "select * from TermBytePosition where term = ?";
        PreparedStatement ps = con.prepareStatement(query);

        ps.setString(1, term);
        TermPositionModel tpm = new TermPositionModel();
        ResultSet rs = ps.executeQuery();
        boolean check = false;

        while (rs.next()) {
            check = true;
            tpm.setId(rs.getInt("id"));
            tpm.setTerm(rs.getString("term"));
            tpm.setBytePosition(rs.getInt("byte_position"));
        }

        if (check == true) {
            return tpm;
        } else
            return null;
    }

    @Override
    public List<TermPositionModel> getTermPositionModels() throws SQLException {
        String query = "select * from TermBytePosition";
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        List<TermPositionModel> ls = new ArrayList<>();

        while (rs.next()) {
            TermPositionModel tpm = new TermPositionModel();
            tpm.setId(rs.getInt("id"));
            tpm.setTerm(rs.getString("term"));
            tpm.setBytePosition(rs.getInt("byte_position"));
            ls.add(tpm);
        }
        return ls;
    }

    @Override
    public void update(TermPositionModel tpm) throws SQLException {
        String query = "update TermBytePosition set term=?, "
                + " byte_position= ? where id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, tpm.getTerm());
        ps.setInt(2, tpm.getBytePosition());
        ps.setInt(3, tpm.getId());
        ps.executeUpdate();
    }
}
