/**
 * Autogenerated by Avro
 *
 * DO NOT EDIT DIRECTLY
 */
package com.opentext.bn.converters.avro.entity;

import org.apache.avro.specific.SpecificData;
import org.apache.avro.message.BinaryMessageEncoder;
import org.apache.avro.message.BinaryMessageDecoder;
import org.apache.avro.message.SchemaStore;

@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class TransactionSplitInfo extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  private static final long serialVersionUID = -1477780521866141179L;
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"TransactionSplitInfo\",\"namespace\":\"com.opentext.bn.converters.avro.entity\",\"fields\":[{\"name\":\"didSplitChildren\",\"type\":\"boolean\"}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }

  private static SpecificData MODEL$ = new SpecificData();

  private static final BinaryMessageEncoder<TransactionSplitInfo> ENCODER =
      new BinaryMessageEncoder<TransactionSplitInfo>(MODEL$, SCHEMA$);

  private static final BinaryMessageDecoder<TransactionSplitInfo> DECODER =
      new BinaryMessageDecoder<TransactionSplitInfo>(MODEL$, SCHEMA$);

  /**
   * Return the BinaryMessageDecoder instance used by this class.
   */
  public static BinaryMessageDecoder<TransactionSplitInfo> getDecoder() {
    return DECODER;
  }

  /**
   * Create a new BinaryMessageDecoder instance for this class that uses the specified {@link SchemaStore}.
   * @param resolver a {@link SchemaStore} used to find schemas by fingerprint
   */
  public static BinaryMessageDecoder<TransactionSplitInfo> createDecoder(SchemaStore resolver) {
    return new BinaryMessageDecoder<TransactionSplitInfo>(MODEL$, SCHEMA$, resolver);
  }

  /** Serializes this TransactionSplitInfo to a ByteBuffer. */
  public java.nio.ByteBuffer toByteBuffer() throws java.io.IOException {
    return ENCODER.encode(this);
  }

  /** Deserializes a TransactionSplitInfo from a ByteBuffer. */
  public static TransactionSplitInfo fromByteBuffer(
      java.nio.ByteBuffer b) throws java.io.IOException {
    return DECODER.decode(b);
  }

   private boolean didSplitChildren;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>.
   */
  public TransactionSplitInfo() {}

  /**
   * All-args constructor.
   * @param didSplitChildren The new value for didSplitChildren
   */
  public TransactionSplitInfo(java.lang.Boolean didSplitChildren) {
    this.didSplitChildren = didSplitChildren;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call.
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return didSplitChildren;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  // Used by DatumReader.  Applications should not call.
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: didSplitChildren = (java.lang.Boolean)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'didSplitChildren' field.
   * @return The value of the 'didSplitChildren' field.
   */
  public java.lang.Boolean getDidSplitChildren() {
    return didSplitChildren;
  }


  /**
   * Creates a new TransactionSplitInfo RecordBuilder.
   * @return A new TransactionSplitInfo RecordBuilder
   */
  public static com.opentext.bn.converters.avro.entity.TransactionSplitInfo.Builder newBuilder() {
    return new com.opentext.bn.converters.avro.entity.TransactionSplitInfo.Builder();
  }

  /**
   * Creates a new TransactionSplitInfo RecordBuilder by copying an existing Builder.
   * @param other The existing builder to copy.
   * @return A new TransactionSplitInfo RecordBuilder
   */
  public static com.opentext.bn.converters.avro.entity.TransactionSplitInfo.Builder newBuilder(com.opentext.bn.converters.avro.entity.TransactionSplitInfo.Builder other) {
    return new com.opentext.bn.converters.avro.entity.TransactionSplitInfo.Builder(other);
  }

  /**
   * Creates a new TransactionSplitInfo RecordBuilder by copying an existing TransactionSplitInfo instance.
   * @param other The existing instance to copy.
   * @return A new TransactionSplitInfo RecordBuilder
   */
  public static com.opentext.bn.converters.avro.entity.TransactionSplitInfo.Builder newBuilder(com.opentext.bn.converters.avro.entity.TransactionSplitInfo other) {
    return new com.opentext.bn.converters.avro.entity.TransactionSplitInfo.Builder(other);
  }

  /**
   * RecordBuilder for TransactionSplitInfo instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<TransactionSplitInfo>
    implements org.apache.avro.data.RecordBuilder<TransactionSplitInfo> {

    private boolean didSplitChildren;

    /** Creates a new Builder */
    private Builder() {
      super(SCHEMA$);
    }

    /**
     * Creates a Builder by copying an existing Builder.
     * @param other The existing Builder to copy.
     */
    private Builder(com.opentext.bn.converters.avro.entity.TransactionSplitInfo.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.didSplitChildren)) {
        this.didSplitChildren = data().deepCopy(fields()[0].schema(), other.didSplitChildren);
        fieldSetFlags()[0] = true;
      }
    }

    /**
     * Creates a Builder by copying an existing TransactionSplitInfo instance
     * @param other The existing instance to copy.
     */
    private Builder(com.opentext.bn.converters.avro.entity.TransactionSplitInfo other) {
            super(SCHEMA$);
      if (isValidValue(fields()[0], other.didSplitChildren)) {
        this.didSplitChildren = data().deepCopy(fields()[0].schema(), other.didSplitChildren);
        fieldSetFlags()[0] = true;
      }
    }

    /**
      * Gets the value of the 'didSplitChildren' field.
      * @return The value.
      */
    public java.lang.Boolean getDidSplitChildren() {
      return didSplitChildren;
    }

    /**
      * Sets the value of the 'didSplitChildren' field.
      * @param value The value of 'didSplitChildren'.
      * @return This builder.
      */
    public com.opentext.bn.converters.avro.entity.TransactionSplitInfo.Builder setDidSplitChildren(boolean value) {
      validate(fields()[0], value);
      this.didSplitChildren = value;
      fieldSetFlags()[0] = true;
      return this;
    }

    /**
      * Checks whether the 'didSplitChildren' field has been set.
      * @return True if the 'didSplitChildren' field has been set, false otherwise.
      */
    public boolean hasDidSplitChildren() {
      return fieldSetFlags()[0];
    }


    /**
      * Clears the value of the 'didSplitChildren' field.
      * @return This builder.
      */
    public com.opentext.bn.converters.avro.entity.TransactionSplitInfo.Builder clearDidSplitChildren() {
      fieldSetFlags()[0] = false;
      return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TransactionSplitInfo build() {
      try {
        TransactionSplitInfo record = new TransactionSplitInfo();
        record.didSplitChildren = fieldSetFlags()[0] ? this.didSplitChildren : (java.lang.Boolean) defaultValue(fields()[0]);
        return record;
      } catch (java.lang.Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumWriter<TransactionSplitInfo>
    WRITER$ = (org.apache.avro.io.DatumWriter<TransactionSplitInfo>)MODEL$.createDatumWriter(SCHEMA$);

  @Override public void writeExternal(java.io.ObjectOutput out)
    throws java.io.IOException {
    WRITER$.write(this, SpecificData.getEncoder(out));
  }

  @SuppressWarnings("unchecked")
  private static final org.apache.avro.io.DatumReader<TransactionSplitInfo>
    READER$ = (org.apache.avro.io.DatumReader<TransactionSplitInfo>)MODEL$.createDatumReader(SCHEMA$);

  @Override public void readExternal(java.io.ObjectInput in)
    throws java.io.IOException {
    READER$.read(this, SpecificData.getDecoder(in));
  }

}
