public class BoundedBox <T extends Number> {
    private T item;

    public BoundedBox(T item)){
        this.item = item;
    }

    public T getItem(){
        return item;
    }
}
