package cecs429.indexing.Database;

import java.sql.SQLException;
import java.util.List;

public interface TermPositionDao {
    public int add(TermPositionModel tpm) throws SQLException;
    public void delete(int id) throws SQLException;

    public TermPositionModel getTermPositionModel(String term) throws SQLException;
    public List<TermPositionModel> getTermPositionModels() throws SQLException;

    public void update(TermPositionModel tpm) throws SQLException;
}
