package misc;

import java.util.Objects;

public class IndexedTuple implements Comparable{

    int freq;
    String fileName;

    public IndexedTuple(int freq,String fileName)
    {
        this.freq = freq;
        this.fileName = fileName;
    }

    public int getFreq() {
        return freq;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexedTuple that = (IndexedTuple) o;
        return fileName.equals(that.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName);
    }

    @Override
    public int compareTo(Object o) {

        IndexedTuple tuple = (IndexedTuple)o;
        return this.freq-tuple.freq;
    }

    @Override
    public String toString() {
        return "IndexedTuple{" +
                "freq=" + freq +
                ", fileName='" + fileName + '\'' +
                '}';
    }

    public static IndexedTuple combineFrequeny(IndexedTuple tuple1, IndexedTuple tuple2)
    {
        if (tuple1.fileName.equals(tuple2.fileName))
        {
            int freq = tuple1.freq*tuple2.freq;
            return new IndexedTuple(freq,tuple1.fileName);
        }
        else
        {
            throw new RuntimeException("Filenames in the tuples are not same , unable to combine");
        }
    }

}
