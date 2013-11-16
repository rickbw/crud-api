package rickbw.crud.util.rx;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import rx.util.functions.Func1;

public final class RxToGuavaFunction<FROM, TO> implements Function<FROM, TO> {

    private final Func1<? super FROM, ? extends TO> func;


    public RxToGuavaFunction(final Func1<? super FROM, ? extends TO> func) {
        this.func = Preconditions.checkNotNull(func, "null function");
    }

    @Override
    @Nullable
    public TO apply(@Nullable final FROM value) {
        @Nullable final TO transformedValue = this.func.call(value);
        return transformedValue;
    }

}
