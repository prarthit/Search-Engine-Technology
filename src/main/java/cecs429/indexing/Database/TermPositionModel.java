package cecs429.indexing.database;

public class TermPositionModel {
    private int id;
    private String term;
    private long byte_position;
  
    public TermPositionModel() {}
  
    public TermPositionModel(String term, int byte_position)
    {
        this.term = term;
        this.byte_position = byte_position;
    }
  
    public int getId()
    {
        return id;
    }
  
    public void setId(int id)
    {
        this.id = id;
    }
  
    public String getTerm()
    {
        return term;
    }
  
    public void setTerm(String term)
    {
        this.term = term;
    }
  
    public long getBytePosition()
    {
        return byte_position;
    }
  
    public void setBytePosition(long byte_position)
    {
        this.byte_position = byte_position;
    }
}
