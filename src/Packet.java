/**
 * Created by SeRG1k on 27.04.16.
 */
public class Packet {

    private int number;
    private String checksum;
    private String data;
    private int size;

    public Packet() {
        this.number=0;
        this.checksum="md5";
        this.data="null";
        this.size=0;
    }
    public Packet(Packet packet) {
        this.number=packet.getNumber();
        this.checksum=packet.getChecksum();
        this.data=packet.getData();
        this.size=packet.getSize();
    }
    public Packet(int number, String checksum, String data, int size, int startPos, int endPos) {
        this.number = number;
        this.checksum = checksum;
        this.data = data;
        this.size = size;
    }


    public void setData(String data) {
        this.data = data;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public int getNumber() {
        return number;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getData() {
        return data;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
