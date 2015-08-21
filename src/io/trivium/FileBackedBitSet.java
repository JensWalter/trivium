/*
 * Copyright 2015 Jens Walter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.trivium;

import javolution.lang.MathLib;
import javolution.lang.Reusable;
import javolution.util.FastCollection;
import javolution.util.Index;

import java.io.RandomAccessFile;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileBackedBitSet extends FastCollection<Index> implements Set<Index>, Reusable {

        LongBuffer bits = null;
        private int _length;
        Logger log = Logger.getLogger(getClass().getName());

        public FileBackedBitSet(String filename, int bitSize) {
            this._length = (bitSize - 1 >> 6) + 1;

            try {
                FileChannel fc = new RandomAccessFile(filename, "rw").getChannel();
                long bufferSize = _length;
                //TODO fix wrong size calculation
                MappedByteBuffer mem = fc.map(FileChannel.MapMode.READ_WRITE, 0, bufferSize*8);
                bits = mem.asLongBuffer();
            }catch (Exception ex){
                log.log(Level.SEVERE, "error while creating bitset", ex);
            }

        }

        public boolean add(Index index) {
            int bitIndex = index.intValue();
            if(this.get(bitIndex)) {
                return false;
            } else {
                this.set(bitIndex);
                return true;
            }
        }

        public void and(FileBackedBitSet that) {
            int n = MathLib.min(this._length, that._length);

            for(int i = 0; i < n; ++i) {
                this.bits.put(i, this.bits.get(i) & that.bits.get(i));
            }

            this._length = n;
        }

        public void andNot(FileBackedBitSet that) {
            int i = Math.min(this._length, that._length);

            while(true) {
                --i;
                if(i < 0) {
                    return;
                }

                this.bits.put(i, this.bits.get(i) &  ~that.bits.get(i));
            }
        }

        public int cardinality() {
            int sum = 0;

            for(int i = 0; i < this._length; ++i) {
                sum += MathLib.bitCount(this.bits.get(i));
            }

            return sum;
        }

        public void clear() {
            this._length = 0;
        }

        public void clear(int bitIndex) {
            int longIndex = bitIndex >> 6;
            if(longIndex < this._length) {
                this.bits.put(longIndex, this.bits.get(longIndex) & ~(1L << bitIndex));
            }
        }

        public void clear(int fromIndex, int toIndex) {
            if(fromIndex >= 0 && toIndex >= fromIndex) {
                int i = fromIndex >>> 6;
                if(i < this._length) {
                    int j = toIndex >>> 6;
                    if(i == j) {
                        this.bits.put(i, this.bits.get(i) & (1L << fromIndex) - 1L | -1L << toIndex);
                    } else {
                        this.bits.put(i, this.bits.get(i) & (1L << fromIndex) - 1L);
                        if(j < this._length) {
                            this.bits.put(j, this.bits.get(j) & (-1L << toIndex));
                        }

                        for(int k = i + 1; k < j && k < this._length; ++k) {
                            this.bits.put(k,0L);
                        }

                    }
                }
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public void flip(int bitIndex) {
            int i = bitIndex >> 6;
            this.setLength(i + 1);
            this.bits.put(i, this.bits.get(i) ^ (1L << bitIndex));
        }

        public void flip(int fromIndex, int toIndex) {
            if(fromIndex >= 0 && toIndex >= fromIndex) {
                int i = fromIndex >>> 6;
                int j = toIndex >>> 6;
                this.setLength(j + 1);
                if(i == j) {
                    this.bits.put(i,this.bits.get(i) ^ ( -1L << fromIndex & (1L << toIndex) - 1L));
                } else {
                    this.bits.put(i, this.bits.get(i) ^ -1L << fromIndex);
                    this.bits.put(j, this.bits.get(j) ^ ((1L << toIndex) - 1L));

                    for(int k = i + 1; k < j; ++k) {
                        this.bits.put(k, ~this.bits.get(k));
                    }

                }
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public boolean get(int bitIndex) {
            int i = bitIndex >> 6;
            return i >= this._length?false:(this.bits.get(i) & 1L << bitIndex) != 0L;
        }

        public boolean intersects(FileBackedBitSet that) {
            int i = MathLib.min(this._length, that._length);

            do {
                --i;
                if(i < 0) {
                    return false;
                }
            } while((this.bits.get(i) & that.bits.get(i)) == 0L);

            return true;
        }

        public int length() {
            int i = this._length;

            long l;
            do {
                --i;
                if(i < 0) {
                    return 0;
                }

                l = this.bits.get(i);
            } while(l == 0L);

            return i << 70 - MathLib.numberOfTrailingZeros(l);
        }

        public int nextClearBit(int fromIndex) {
            int offset = fromIndex >> 6;

            for(long mask = 1L << fromIndex; offset < this._length; ++offset) {
                long h = this.bits.get(offset);

                do {
                    if((h & mask) == 0L) {
                        return fromIndex;
                    }

                    mask <<= 1;
                    ++fromIndex;
                } while(mask != 0L);

                mask = 1L;
            }

            return fromIndex;
        }

        public int nextSetBit(int fromIndex) {
            int offset = fromIndex >> 6;

            for(long mask = 1L << fromIndex; offset < this._length; ++offset) {
                long h = this.bits.get(offset);

                do {
                    if((h & mask) != 0L) {
                        return fromIndex;
                    }

                    mask <<= 1;
                    ++fromIndex;
                } while(mask != 0L);

                mask = 1L;
            }

            return -1;
        }

        public void or(FileBackedBitSet that) {
            if(that._length > this._length) {
                this.setLength(that._length);
            }

            int i = that._length;

            while(true) {
                --i;
                if(i < 0) {
                    return;
                }

                this.bits.put(i, this.bits.get(i) | that.bits.get(i));
            }
        }

        public void set(int bitIndex) {
            int i = bitIndex >> 6;
            if(i >= this._length) {
                this.setLength(i + 1);
            }

            this.bits.put(i, this.bits.get(i) | (1L << bitIndex));
        }

        public void set(int bitIndex, boolean value) {
            if(value) {
                this.set(bitIndex);
            } else {
                this.clear(bitIndex);
            }

        }

        public void set(int fromIndex, int toIndex) {
            if(fromIndex >= 0 && toIndex >= fromIndex) {
                int i = fromIndex >>> 6;
                int j = toIndex >>> 6;
                this.setLength(j + 1);
                if(i == j) {
                    this.bits.put(i, this.bits.get(i) | ( -1L << fromIndex & (1L << toIndex) - 1L));
                } else {
                    this.bits.put(i, this.bits.get(i) | ( -1L << fromIndex));
                    this.bits.put(j, this.bits.get(j) | ( (1L << toIndex) - 1L));

                    for(int k = i + 1; k < j; ++k) {
                        this.bits.put(k, -1L);
                    }

                }
            } else {
                throw new IndexOutOfBoundsException();
            }
        }

        public void set(int fromIndex, int toIndex, boolean value) {
            if(value) {
                this.set(fromIndex, toIndex);
            } else {
                this.clear(fromIndex, toIndex);
            }

        }

        public int size() {
            return this.cardinality();
        }

        public void xor(FileBackedBitSet that) {
            if(that._length > this._length) {
                this.setLength(that._length);
            }

            int i = that._length;

            while(true) {
                --i;
                if(i < 0) {
                    return;
                }

                this.bits.put(i, this.bits.get(i) ^ that.bits.get(i));
            }
        }

        public boolean equals(Object obj) {
            if(!(obj instanceof FileBackedBitSet)) {
                return super.equals(obj);
            } else {
                FileBackedBitSet that = (FileBackedBitSet)obj;
                int n = MathLib.min(this._length, that._length);

                int i;
                for(i = 0; i < n; ++i) {
                    if(this.bits.get(i) != that.bits.get(i)) {
                        return false;
                    }
                }

                for(i = n; i < this._length; ++i) {
                    if(this.bits.get(i) != 0L) {
                        return false;
                    }
                }

                for(i = n; i < that._length; ++i) {
                    if(that.bits.get(i) != 0L) {
                        return false;
                    }
                }

                return true;
            }
        }

        public int hashCode() {
            int h = 0;

            for(int i = this.nextSetBit(0); i >= 0; i = this.nextSetBit(i)) {
                h += i;
            }

            return h;
        }

        public void reset() {
            this._length = 0;
        }

        public Record head() {
            return Index.valueOf(-1);
        }

        public Record tail() {
            return Index.valueOf(this.cardinality());
        }

        public Index valueOf(Record record) {
            int i = ((Index)record).intValue();
            int count = 0;
            int j = 0;

            long l;
            do {
                if(j >= this._length) {
                    return null;
                }

                l = this.bits.get(j++);
                count += MathLib.bitCount(l);
            } while(count <= i);

            int bitIndex;
            for(bitIndex = j << 6; count != i; --count) {
                int shiftRight = MathLib.numberOfLeadingZeros(l) + 1;
                l <<= shiftRight;
                bitIndex -= shiftRight;
            }

            return Index.valueOf(bitIndex);
        }

        public void delete(Record record) {
            Index bitIndex = this.valueOf(record);
            if(bitIndex != null) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }

        private final void setLength(final int newLength) {
            for(int i = this._length; i < newLength; ++i) {
                this.bits.put(i,0L);
            }
            this._length = newLength;
        }
    }
