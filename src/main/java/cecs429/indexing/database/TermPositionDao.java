package cecs429.indexing.database;

import java.sql.SQLException;
import java.util.List;

public interface TermPositionDao {
    public int add(TermPositionModel tpm) throws SQLException;
    public TermPositionModel getTermPositionModel(String term) throws SQLException;
    public List<String> getVocabularyTerm() throws SQLException;
    public void update(TermPositionModel tpm) throws SQLException;
}
