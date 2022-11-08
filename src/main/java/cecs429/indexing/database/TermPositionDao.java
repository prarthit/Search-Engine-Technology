package cecs429.indexing.database;

import java.sql.SQLException;
import java.util.List;

public interface TermPositionDao {
    public void add(TermPositionModel tpm) throws SQLException;
    public void add(String term, long byte_position) throws SQLException;
    public TermPositionModel getTermPositionModel(String term) throws SQLException;
    public List<String> getVocabularyTerm() throws SQLException;
    public void update(TermPositionModel tpm) throws SQLException;
}
