package org.example;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;

import static java.lang.foreign.ValueLayout.ADDRESS;

public class FfiScratchWork {
    public static void main(String[] args) {
        Arena allocator = Arena.global();
        Linker linker = Linker.nativeLinker();
        SymbolLookup lib = linker.defaultLookup();

        /*
            Think of this as the equivalent of a C pointer.
            Anytime a C function returns a pointer,
            or takes in a pointer argument
            MemorySegment is used to represent it in java code.

            Recall that "lib" contains all the common libraries
            that are typically found on a PC,
            and the C standard library is one of them.

            The code finds the address (pointer) of where "fopen" is stored,
            and returns it, or throws an exception if it wasn’t found.
        */
        MemorySegment fOpenAddress = lib.find("fopen").orElseThrow();

        /*
            FILE *fopen(const char *filename, const char *mode)

            The code is exactly what it sounds like.
            It describes the signature of fopen.

            The first argument is the return type of fopen - A memory address.
            The 2nd and 3rd (and subsequent) arguments are the parameters.
            They’re all memory addresses.
            There’s also a FunctionDescriptor.ofVoid()
            that only allows parameters, and returns void
        */
        FunctionDescriptor fOpenDesc =
                FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS);

        /*
            This basically creates a way to actually call the function.
            In general, it is used to call a foreign function in any language.
         */
        MethodHandle fOpen = linker.downcallHandle(fOpenAddress, fOpenDesc);

        String filePath = "";
        String mode = "r";

        MemorySegment pathPtr = new AllocateFromArena(allocator).allocateFrom(filePath);
        MemorySegment modePtr = new AllocateFromArena(allocator).allocateFrom(mode);

        MemorySegment result;
        try {
            result = (MemorySegment) fOpen.invoke(pathPtr, modePtr);
        } catch (Throwable e) {
            result = MemorySegment.NULL;
        }
    }

    @RequiredArgsConstructor
    static class AllocateFromArena {
        @NonNull
        @Delegate
        private final Arena arena;

        public MemorySegment allocateFrom(String str) {
            return arena.allocateUtf8String(str);
        }
    }
}
