/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef MTK_MPEG2_TS_EXTRACTOR_H_

#define MTK_MPEG2_TS_EXTRACTOR_H_

#include <media/stagefright/foundation/ABase.h>
#include <media/MediaExtractor.h>
#include <media/MediaTrack.h>
#include <media/stagefright/MetaDataBase.h>
#include <utils/threads.h>
#include <utils/KeyedVector.h>
#include <utils/Vector.h>

#include "mpeg2ts/ATSParser.h"

namespace android {

struct AMessage;
struct AnotherPacketSource;
struct ATSParser;
class DataSourceBase;
struct MtkMPEG2TSSource;
class String8;

struct MtkMPEG2TSExtractor : public MediaExtractor {
    explicit MtkMPEG2TSExtractor(DataSourceBase *source);

    virtual size_t countTracks();
    virtual MediaTrack *getTrack(size_t index);
    virtual status_t getTrackMetaData(MetaDataBase &meta, size_t index, uint32_t flags);

    virtual status_t getMetaData(MetaDataBase& meta);

    virtual status_t setMediaCas(const uint8_t* /*casToken*/, size_t /*size*/) override;

    virtual uint32_t flags() const;
    virtual const char * name() { return "MtkMPEG2TSExtractor"; }

private:
    friend struct MtkMPEG2TSSource;

    mutable Mutex mLock;

    DataSourceBase *mDataSource;

    sp<ATSParser> mParser;

    // Used to remember SyncEvent occurred in feedMore() when called from init(),
    // because init() needs to update |mSourceImpls| before adding SyncPoint.
    ATSParser::SyncEvent mLastSyncEvent;

    Vector<sp<AnotherPacketSource> > mSourceImpls;

    Vector<KeyedVector<int64_t, off64_t> > mSyncPoints;
    // Sync points used for seeking --- normally one for video track is used.
    // If no video track is present, audio track will be used instead.
    KeyedVector<int64_t, off64_t> *mSeekSyncPoints;

    off64_t mOffset;

    static bool isScrambledFormat(MetaDataBase &format);

    void init();
    void addSource(const sp<AnotherPacketSource> &impl);
    // Try to feed more data from source to parser.
    // |isInit| means this function is called inside init(). This is a signal to
    // save SyncEvent so that init() can add SyncPoint after it updates |mSourceImpls|.
    // This function returns OK if expected amount of data is fed from DataSourceBase to
    // parser and is successfully parsed. Otherwise, various error codes could be
    // returned, e.g., ERROR_END_OF_STREAM, or no data availalbe from DataSourceBase, or
    // the data has syntax error during parsing, etc.
    status_t feedMore(bool isInit = false);
    status_t seek(int64_t seekTimeUs,
            const MediaSource::ReadOptions::SeekMode& seekMode);
    status_t queueDiscontinuityForSeek(int64_t actualSeekTimeUs);
    status_t seekBeyond(int64_t seekTimeUs);

    status_t feedUntilBufferAvailable(const sp<AnotherPacketSource> &impl);

    // Add a SynPoint derived from |event|.
    void addSyncPoint_l(const ATSParser::SyncEvent &event);

    status_t  estimateDurationsFromTimesUsAtEnd();

    DISALLOW_EVIL_CONSTRUCTORS(MtkMPEG2TSExtractor);

// add for mtk
    // add for support M2TS file playback
    size_t kFillPacketSize;
// #ifdef MTK_SEEK_AND_DURATION
public:
    // add for mtk seek method
    void seekTo(int64_t seekTimeUs);
    bool getSeeking();
    bool IsLocalSource();  // http streaming seek is needed
    void setVideoState(bool state);
    bool getVideoState(void);
private:
    // add for mtk duration calculate method and local seek
    int64_t mDurationUs;
    off64_t mFileSize;
    // add for mtk duration calculate method
    bool mFindingMaxPTS;
    off64_t mOffsetPAT;
    // add for mtk seek method
    int64_t mSeekTimeUs;
    bool mSeeking;
    off64_t mSeekingOffset;
    off64_t mMinOffset;
    off64_t mMaxOffset;
    uint64_t mMaxcount;
    bool mVideoUnSupportedByDecoder;
    bool End_OF_FILE;

    // add for mtk duration calculate method and local seek
     bool findSyncWord(DataSourceBase *source, off64_t StartOffset,
            uint64_t size, size_t PacketSize, off64_t &NewOffset);
    status_t parseMaxPTS();
    // add for mtk duration calculate method
    uint64_t getDurationUs();
    // add to avoid find sync word err when seek.
    int32_t findSyncCode(const void *data, size_t size);
// #endif
// end of add for mtk
};

bool MtkSniffMPEG2TS(DataSourceBase *source, float *confidence);

}  // namespace android

#endif  // MTK_MPEG2_TS_EXTRACTOR_H_
