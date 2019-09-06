package codingdavinci.tour.filter;

public interface Filter<T> {
    public boolean accept(T t);
    public void clear();
    public void refresh();
}
